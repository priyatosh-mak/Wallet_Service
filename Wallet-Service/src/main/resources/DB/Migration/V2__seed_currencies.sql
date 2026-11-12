INSERT INTO currencies (code, name, minor_units)
VALUES ('ZMW', 'Zambian Kwacha', 2)
ON CONFLICT (code) DO NOTHING;
