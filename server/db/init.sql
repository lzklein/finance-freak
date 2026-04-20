DROP TABLE IF EXISTS alert_history CASCADE;
DROP TABLE IF EXISTS alerts CASCADE;
DROP TABLE IF EXISTS price_cache CASCADE;
DROP TABLE IF EXISTS watchlist_assets CASCADE;
DROP TABLE IF EXISTS watchlists CASCADE;
DROP TABLE IF EXISTS skin_details CASCADE;
DROP TABLE IF EXISTS assets CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) NOT NULL UNIQUE,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  verification_token VARCHAR(255),
  verification_token_expires_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE assets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL UNIQUE,
  asset_type VARCHAR(20) NOT NULL CHECK (asset_type IN ('STOCK', 'CRYPTO', 'CS2_SKIN')),
  image_url VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE skin_details (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE UNIQUE,
  weapon_type VARCHAR(50),
  rarity VARCHAR(20) CHECK (rarity IN ('CONSUMER', 'INDUSTRIAL', 'MIL_SPEC', 'RESTRICTED', 'CLASSIFIED', 'COVERT', 'CONTRABAND')),
  exterior VARCHAR(20) CHECK (exterior IN ('FN', 'MW', 'FT', 'WW', 'BS')),
  is_stattrak BOOLEAN NOT NULL DEFAULT FALSE,
  is_souvenir BOOLEAN NOT NULL DEFAULT FALSE,
  float_min NUMERIC(10, 8),
  float_max NUMERIC(10, 8)
);

CREATE TABLE market_details (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE UNIQUE,
  symbol VARCHAR(20) NOT NULL,
  exchange VARCHAR(50),
  asset_class VARCHAR(20),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE watchlists (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE watchlist_assets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  watchlist_id UUID NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
  asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
  added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  entry_price NUMERIC(18, 2),
  UNIQUE(watchlist_id, asset_id)
);

CREATE TABLE price_cache (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
  marketplace VARCHAR(20) NOT NULL CHECK (marketplace IN ('STEAM', 'SKINPORT', 'BUFF', 'CSFLOAT', 'SKINBARON', 'NASDAQ', 'NYSE', 'CRYPTO')),
  lowest_ask NUMERIC(18, 2),
  highest_bid NUMERIC(18, 2),
  last_sale NUMERIC(18, 2),
  volume_24h INT,
  fetched_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(asset_id, marketplace)
);
CREATE INDEX idx_price_cache_asset ON price_cache(asset_id);
CREATE INDEX idx_price_cache_fetched ON price_cache(fetched_at DESC);

CREATE TABLE alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
  marketplace VARCHAR(20),
  condition VARCHAR(10) NOT NULL CHECK (condition IN ('ABOVE', 'BELOW')),
  threshold NUMERIC(18, 2) NOT NULL,
  price_type VARCHAR(10) NOT NULL DEFAULT 'ASK' CHECK (price_type IN ('ASK', 'BID', 'LAST')),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_triggered_at TIMESTAMPTZ
);

CREATE TABLE alert_history (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  alert_id UUID NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  triggered_price NUMERIC(18, 2) NOT NULL,
  marketplace VARCHAR(20),
  triggered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_alert_history_user ON alert_history(user_id, triggered_at DESC);