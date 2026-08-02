-- ══════════════════════════════════════
-- DEVICE SESSIONS TABLE
-- Run this in Supabase SQL Editor
-- ══════════════════════════════════════

-- Drop old table if exists (to update schema)
DROP TABLE IF EXISTS public.device_sessions;

CREATE TABLE public.device_sessions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    device_id TEXT NOT NULL,
    session_token TEXT NOT NULL,
    device_hash TEXT NOT NULL,
    device_model TEXT,
    os_version TEXT,
    app_version TEXT,
    expires_at TIMESTAMPTZ NOT NULL,
    last_verified_at TIMESTAMPTZ DEFAULT now(),
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user_id, device_id)
);

-- Indexes
CREATE INDEX idx_device_sessions_lookup
    ON public.device_sessions(user_id, device_id, session_token);

CREATE INDEX idx_device_sessions_expiry
    ON public.device_sessions(expires_at);

-- RLS
ALTER TABLE public.device_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own sessions"
    ON public.device_sessions FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Service role full access"
    ON public.device_sessions FOR ALL
    USING (true)
    WITH CHECK (true);
