-- Default limits are intentionally left NULL: wallets fall back to 0 credit / no transaction cap
-- until the business supplies real values (update the rows in a later migration).
INSERT INTO wallet_types (code, name, description, can_extend_credit) VALUES
    ('CUSTOMER',     'Customer',     'Individual customer wallet',            FALSE),
    ('ORGANIZATION', 'Organization', 'Company / employer wallet',             FALSE),
    ('PARTNER',      'Partner',      'Partner wallet (can extend credit)',    TRUE),
    ('SYSTEM',       'System',       'Internal / operational wallet',         FALSE)
ON CONFLICT (code) DO NOTHING;
