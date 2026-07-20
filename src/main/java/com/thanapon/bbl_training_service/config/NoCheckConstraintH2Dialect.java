package com.thanapon.bbl_training_service.config;

import org.hibernate.dialect.H2Dialect;

// Hibernate 7 auto-generates a CHECK (col IN (...)) constraint for every converted-enum column
// (see EnumJavaType#getCheckCondition), gated by Dialect#supportsColumnCheck(). On this project's
// H2 version, that inline CHECK combined with the extra ALTER TABLE statements Hibernate issues
// under ddl-auto=update (dropping/recreating the unique constraint on username) leaves the CHECK
// permanently broken: every subsequent insert is rejected regardless of value, even values that
// satisfy the constraint text. Dropping the constraint at runtime confirms this - inserts succeed
// immediately after. Disabling column-check generation avoids the corrupted constraint entirely;
// application-level validation (the Role enum, DTO validation) still enforces valid values.
public class NoCheckConstraintH2Dialect extends H2Dialect {

    @Override
    public boolean supportsColumnCheck() {
        return false;
    }
}
