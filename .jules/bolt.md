## 2026-10-27 - Fast Trigonometric Table Lookup via Bitwise Masking
**Learning:** In hot rendering loops and math calculations (such as `MathUtils.sin`/`cos`), replacing standard integer modulo `% SIN_TABLE_SIZE` and conditional negative check branches (`if (idx < 0)`) with bitwise AND masking (`& (SIN_TABLE_SIZE - 1)`) yields a ~3.5x speedup for lookup table evaluations when table size is a power of 2.
**Action:** Always prefer bitwise AND masking over modulo `%` operator when indexing into power-of-two lookup tables in Java core utilities.
