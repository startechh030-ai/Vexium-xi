-- ══════════════════════════════════════
-- VEXIUM DATABASE SCHEMA
-- Run this in Supabase SQL Editor
-- ══════════════════════════════════════

-- ══════════════════════════════════════
-- STEP 1: HELPER FUNCTIONS (must come first)
-- ══════════════════════════════════════

-- Generate a unique 8-char referral code
CREATE OR REPLACE FUNCTION generate_referral_code()
RETURNS TEXT AS $$
DECLARE
    code TEXT;
    exists_already BOOLEAN;
BEGIN
    LOOP
        code := upper(substr(md5(random()::text), 1, 8));
        SELECT EXISTS(SELECT 1 FROM public.profiles WHERE referral_code = code) INTO exists_already;
        EXIT WHEN NOT exists_already;
    END LOOP;
    RETURN code;
END;
$$ LANGUAGE plpgsql;

-- Auto-update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ══════════════════════════════════════
-- STEP 2: TABLES
-- ══════════════════════════════════════

-- ── PROFILES TABLE ──
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE,
    full_name TEXT,
    avatar_url TEXT,
    referral_code TEXT UNIQUE DEFAULT generate_referral_code(),
    referred_by UUID REFERENCES public.profiles(id),
    is_email_verified BOOLEAN DEFAULT false,
    quick_login_pin_hash TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ── USER RANKS TABLE ──
CREATE TABLE IF NOT EXISTS public.user_ranks (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    rank_level INT4 DEFAULT 1,
    rank_name TEXT DEFAULT 'Beginner',
    xp_earned NUMERIC DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ══════════════════════════════════════
-- STEP 3: TRIGGERS
-- ══════════════════════════════════════

-- Auto-create profile + rank when a new user signs up
CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, full_name, avatar_url)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data ->> 'full_name', NEW.raw_user_meta_data ->> 'name'),
        COALESCE(NEW.raw_user_meta_data ->> 'avatar_url', NEW.raw_user_meta_data ->> 'picture')
    );

    INSERT INTO public.user_ranks (user_id)
    VALUES (NEW.id);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger on auth.users insert
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION handle_new_user();

-- Auto-update timestamps
DROP TRIGGER IF EXISTS profiles_updated_at ON public.profiles;
CREATE TRIGGER profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

DROP TRIGGER IF EXISTS user_ranks_updated_at ON public.user_ranks;
CREATE TRIGGER user_ranks_updated_at
    BEFORE UPDATE ON public.user_ranks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

-- ══════════════════════════════════════
-- STEP 4: ROW LEVEL SECURITY (RLS)
-- ══════════════════════════════════════

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_ranks ENABLE ROW LEVEL SECURITY;

-- Profiles: anyone can read, only owner can update/insert
CREATE POLICY "Profiles are viewable by everyone"
    ON public.profiles FOR SELECT
    USING (true);

CREATE POLICY "Users can update their own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can insert their own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- User Ranks: anyone can read, only owner can update/insert
CREATE POLICY "Ranks are viewable by everyone"
    ON public.user_ranks FOR SELECT
    USING (true);

CREATE POLICY "Users can update their own rank"
    ON public.user_ranks FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can insert their own rank"
    ON public.user_ranks FOR INSERT
    WITH CHECK (auth.uid() = user_id);
