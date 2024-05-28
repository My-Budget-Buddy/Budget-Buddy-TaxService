DROP TABLE IF EXISTS tax_brackets CASCADE;
DROP TABLE IF EXISTS standard_deduction CASCADE;
DROP TABLE IF EXISTS capital_gains_tax;
DROP TABLE IF EXISTS filing_status CASCADE;
DROP TABLE IF EXISTS child_tax_credit CASCADE;
DROP TABLE IF EXISTS dependent_care_tax_credit CASCADE;
DROP TABLE IF EXISTS dependent_care_tax_credit_limit CASCADE;
DROP TABLE IF EXISTS earned_income_tax_credit CASCADE;
DROP TABLE IF EXISTS education_tax_credit_aotc CASCADE;
DROP TABLE IF EXISTS education_tax_credit_llc CASCADE;
DROP TABLE IF EXISTS savers_tax_credit CASCADE;
DROP TABLE IF EXISTS state_tax CASCADE;
DROP TABLE IF EXISTS states CASCADE;
DROP TABLE IF EXISTS deduction CASCADE;
DROP TABLE IF EXISTS tax_return CASCADE;
DROP TABLE IF EXISTS taxreturn_deduction CASCADE;
DROP TABLE IF EXISTS w2 CASCADE;
DROP TABLE IF EXISTS other_income CASCADE;
DROP TABLE IF EXISTS taxreturn_credit CASCADE;


CREATE TABLE IF NOT EXISTS child_tax_credit (
  id SERIAL PRIMARY KEY,
  per_qualifying_child INT NOT NULL,
  per_other_child INT NOT NULL,
  income_threshold INT NOT NULL,
  rate_factor DECIMAL(5, 2) NOT NULL DEFAULT 0.05,
  refundable BOOLEAN NOT NULL,
  refund_limit INT NOT NULL,
  refund_rate DECIMAL(5, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS dependent_care_tax_credit (
  id SERIAL PRIMARY KEY,
  income_range INT NOT NULL,
  rate DECIMAL(5, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS dependent_care_tax_credit_limit (
  id SERIAL PRIMARY KEY,
  num_dependents INT NOT NULL,
  credit_limit INT NOT NULL,
  refundable BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS earned_income_tax_credit (
  id SERIAL PRIMARY KEY,
  agi_threshold_3children INT NOT NULL,
  agi_threshold_2Children INT NOT NULL,
  agi_threshold_1Children INT NOT NULL,
  agi_threshold_0Children INT NOT NULL,
  amount_3children INT NOT NULL,
  amount_2children INT NOT NULL,
  amount_1children INT NOT NULL,
  amount_0children INT NOT NULL,
  investment_income_limit INT NOT NULL,
  refundable BOOLEAN NOT NULL,
  refund_limit INT NOT NULL,
  refund_rate DECIMAL(5, 2)
);

CREATE TABLE IF NOT EXISTS education_tax_credit_aotc (
  id SERIAL PRIMARY KEY,
  full_credit_income_threshold INT NOT NULL,
  partial_credit_income_threshold INT NOT NULL,
  income_partial_credit_rate DECIMAL(5, 2) NOT NULL,
  max_credit_amount INT NOT NULL,
  full_credit_expenses_threshold INT NOT NULL,
  partial_credit_expenses_threshold INT NOT NULL,
  partial_credit_expenses_rate DECIMAL(5,2) NOT NULL,
  refundable BOOLEAN NOT NULL,
  refund_limit INT NOT NULL,
  refund_rate DECIMAL(5, 2)
);

CREATE TABLE IF NOT EXISTS education_tax_credit_llc (
  id SERIAL PRIMARY KEY,
  full_credit_income_threshold INT NOT NULL,
  partial_credit_income_threshold INT NOT NULL,
  income_partial_credit_rate DECIMAL(5, 2) NOT NULL,
  max_credit_amount INT NOT NULL,
  expenses_threshold INT NOT NULL,
  credit_rate DECIMAL(5, 2) NOT NULL,
  refundable BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS savers_tax_credit (
  id SERIAL PRIMARY KEY,
  agi_threshold_first_contribution_limit INT NOT NULL,
  agi_threshold_second_contribution_limit INT NOT NULL,
  agi_threshold_third_contribution_limit INT NOT NULL,
  first_contribution_rate DECIMAL(5, 2) NOT NULL,
  second_contribution_rate DECIMAL(5, 2) NOT NULL,
  third_contribution_rate DECIMAL(5, 2) NOT NULL,
  max_contribution_amount INT NOT NULL,
  refundable BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS filing_status (
  id SERIAL PRIMARY KEY,
  status VARCHAR(50) NOT NULL UNIQUE,
  child_tax_credit_id INT NOT NULL,
  earned_income_tax_credit_id INT NOT NULL,
  education_tax_credit_aotc_id INT NOT NULL,
  education_tax_credit_llc_id INT NOT NULL,
  savers_tax_credit_id INT NOT NULL,
  FOREIGN KEY (child_tax_credit_id) REFERENCES child_tax_credit(id),
  FOREIGN KEY (earned_income_tax_credit_id) REFERENCES earned_income_tax_credit(id),
  FOREIGN KEY (education_tax_credit_aotc_id) REFERENCES education_tax_credit_aotc(id),
  FOREIGN KEY (education_tax_credit_llc_id) REFERENCES education_tax_credit_llc(id),
  FOREIGN KEY (savers_tax_credit_id) REFERENCES savers_tax_credit(id)
);

CREATE TABLE IF NOT EXISTS standard_deduction (
  id SERIAL PRIMARY KEY,
  filing_status_id INT NOT NULL,
  deduction_amount INT NOT NULL,
  FOREIGN KEY (filing_status_id) REFERENCES filing_status(id)
);

CREATE TABLE IF NOT EXISTS capital_gains_tax (
  id SERIAL PRIMARY KEY,
  filing_status_id INT NOT NULL,
  rate DECIMAL(5, 2) NOT NULL,
  income_range INT NOT NULL,
  FOREIGN KEY (filing_status_id) REFERENCES filing_status(id)
);

CREATE TABLE IF NOT EXISTS tax_brackets (
  id SERIAL PRIMARY KEY,
  filing_status_id INT NOT NULL,
  rate DECIMAL(5, 2) NOT NULL,
  min_income INT NOT NULL,
  max_income INT NOT NULL,
  FOREIGN KEY (filing_status_id) REFERENCES filing_status(id)
);

CREATE TABLE IF NOT EXISTS states (
  id SERIAL PRIMARY KEY,
  state_name VARCHAR(50),
  state_code VARCHAR(2)
);

CREATE TABLE IF NOT EXISTS state_tax (
  id SERIAL PRIMARY KEY,
  state_id INT NOT NULL,
  income_range INT NOT NULL,
  rate DECIMAL(6, 5) NOT NULL,
  FOREIGN KEY (state_id) REFERENCES states(id)
);

CREATE TABLE IF NOT EXISTS deduction (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    agi_limit DECIMAL(10, 3),
    itemized BOOLEAN
);

CREATE TABLE IF NOT EXISTS tax_return (
    id SERIAL PRIMARY KEY,
    years INT,
    filing_status INT,
    user_id INT,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(50),
    phone_number VARCHAR(20),
    address VARCHAR(50),
    city VARCHAR(50),
    state VARCHAR(2),
    zip VARCHAR(10),
    date_of_birth DATE,
    ssn VARCHAR(11),
    total_income NUMERIC,
    adjusted_gross_income NUMERIC,
    taxable_income NUMERIC,
    fed_tax_withheld NUMERIC,
    state_tax_withheld NUMERIC,
    social_security_tax_withheld NUMERIC,
    medicare_tax_withheld NUMERIC,
    total_credits NUMERIC,
    federal_refund NUMERIC,
    state_refund NUMERIC,
    CONSTRAINT unique_year_user_id UNIQUE (years, user_id)
);

CREATE TABLE IF NOT EXISTS taxreturn_deduction (
    id SERIAL PRIMARY KEY,
    taxreturn_id INT,
    deduction_id INT,
    amount_spent NUMERIC,
    CONSTRAINT fk_taxreturn FOREIGN KEY (taxreturn_id) REFERENCES tax_return(id),
    CONSTRAINT fk_deduction FOREIGN KEY (deduction_id) REFERENCES deduction(id),
    CONSTRAINT unique_taxreturn_deduction UNIQUE (taxreturn_id, deduction_id)
);

CREATE TABLE IF NOT EXISTS w2 (
    id SERIAL PRIMARY KEY,
    tax_return_id INT,
    years INT,
    user_id INT,
    employer VARCHAR(50),
    wages NUMERIC DEFAULT 0,
    state INT,
    federal_income_tax_withheld NUMERIC,
    state_income_tax_withheld NUMERIC,
    social_security_tax_withheld NUMERIC,
    medicare_tax_withheld NUMERIC,
    image_key VARCHAR(50),
    CONSTRAINT fk_tax_return FOREIGN KEY (tax_return_id) REFERENCES tax_return(id)
);

CREATE TABLE IF NOT EXISTS other_income (
  id SERIAL PRIMARY KEY,
  tax_return_id INT NOT NULL,
  long_term_capital_gains DECIMAL,
  short_term_capital_gains DECIMAL,
  other_investment_income DECIMAL,
  net_business_income DECIMAL,
  additional_income DECIMAL,
  FOREIGN KEY (tax_return_id) REFERENCES tax_return(id)
);

CREATE TABLE IF NOT EXISTS taxreturn_credit (
  id SERIAL PRIMARY KEY,
  tax_return_id INT NOT NULL,
  num_dependents INT,
  num_dependents_aotc INT,
  num_dependents_under_13 INT,
  child_care_expenses DECIMAL,
  education_expenses DECIMAL,
  llc_education_expenses DECIMAL,
  ira_contributions DECIMAL,
  claimed_as_dependent BOOLEAN,
  llc_credit BOOLEAN,
  FOREIGN KEY (tax_return_id) REFERENCES tax_return(id)
);


