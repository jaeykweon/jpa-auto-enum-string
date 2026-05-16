# Why Hibernate 6.5 compatibility requires reflection on BasicValue

## Background

The library uses the Hibernate `Integrator` SPI to apply `EnumType.STRING` mapping to
unannotated enum fields. The integrator runs during `SessionFactory` construction, after
entity metadata has been built.

In Hibernate 6.4 and earlier, `BasicValue.resolution` is computed lazily — it is `null`
when the integrator runs. Calling `basicValue.setEnumerationStyle(EnumType.STRING)` alone
is enough: when `resolve()` is called later, it builds a fresh STRING resolution.

**Hibernate 6.5 changed this**: `BasicValue.resolution` is now computed eagerly during
metadata building, before the integrator runs.

## The problem: three caches, all stale after the version bump

By the time our integrator runs in H6.5, ORDINAL information has been written into three
places:

| Location | Field | Stale value | Guards against overwrite |
|---|---|---|---|
| `BasicValue` | `resolution` | `OrdinalEnumResolution` | checked manually by `resolve()` — skipped if non-null |
| `Column` | `sqlTypeCode` | `Types.TINYINT` | `resolveColumn()` has an explicit `if (getSqlTypeCode() == null)` guard |
| `Column` | `checkConstraints` | `[check (status between 0 and 3)]` | `getCheckConstraints()` returns `Collections.unmodifiableList(...)` |

`setEnumerationStyle(STRING)` alone has no effect because the cached `resolution` is
returned before `buildResolution()` is ever called again.

## The fix: clear all three, then re-resolve

```java
// 1. Tell BasicValue we want STRING
basicValue.setEnumerationStyle(EnumType.STRING);

// 2. Clear Column.sqlTypeCode so resolveColumn() will re-derive the DDL type
Field sqlTypeCodeField = Column.class.getDeclaredField("sqlTypeCode");
sqlTypeCodeField.setAccessible(true);
sqlTypeCodeField.set(column, null);

// 3. Clear BasicValue.resolution so resolve() calls buildResolution() again
Field resolutionField = BasicValue.class.getDeclaredField("resolution");
resolutionField.setAccessible(true);
resolutionField.set(basicValue, null);

// 4. Re-resolve: buildResolution() now sees STRING style → sets VARCHAR/ENUM column type
basicValue.resolve();

// 5. Clear the stale ordinal range check ("between 0 and 3").
//    getCheckConstraints() returns an unmodifiable view, so we reflect into the raw list.
Field checkConstraintsField = Column.class.getDeclaredField("checkConstraints");
checkConstraintsField.setAccessible(true);
List<?> checkConstraints = (List<?>) checkConstraintsField.get(column);
if (checkConstraints != null) {
    checkConstraints.clear();
}
```

Step 5 must happen **after** `resolve()`, not before. Clearing the list before re-resolve
causes the column type to revert to TINYINT — the re-resolve detects no constraints and
falls back to the cached ORDINAL path.

## Why not use a public API?

There is no public Hibernate API to invalidate a cached `BasicValue.resolution` or remove
a column's check constraints post-build. The `Integrator` SPI is explicitly designed for
this kind of post-metadata hook, but H6.5's eager resolution made the window too late for
the public API.

`MetadataBuilderContributor` runs before entity scanning, so it cannot filter by entity
package. `TypeContributor` applies globally to all enums with no per-field control.
Neither is a suitable replacement.

## Risk and tested versions

This approach accesses three private fields by name. If Hibernate renames or removes any
of them in a future release, `applyStringMapping()` will throw and fall back gracefully
(the exception is caught and logged; the field is left as ORDINAL).

| Hibernate version | `BasicValue.resolution` behavior | Tested |
|---|---|---|
| 6.4.x | Lazy (null at integrator time) — reflection branch not taken | ✓ via `tests/integration` |
| 6.5.x | Eager — full reflection path required | ✓ via `tests/integration-jakarta` |
| 7.x | Expected same as 6.5 (unverified) | pending |
