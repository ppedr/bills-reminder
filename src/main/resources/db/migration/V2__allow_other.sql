DROP TABLE IF EXISTS bill;

CREATE TABLE bill (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    due_date DATE,
    email TEXT,
    type TEXT CHECK (type IN ('INTERNET','WATER','CELLPHONE_PLAN','MEI','ELECTRICITY','GAS','CREDIT_CARD','OTHER')),
    credit_card_name TEXT CHECK (credit_card_name IN ('INTER','ITAU','XP','PICPAY','OTHER')),
    paid INTEGER NOT NULL DEFAULT 0
);
