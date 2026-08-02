import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(
        JSON.stringify({ valid: false, error: "No auth header" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

    // Verify JWT
    const token = authHeader.replace("Bearer ", "");
    const { data: { user }, error } = await supabase.auth.getUser(token);

    if (error || !user) {
      return new Response(
        JSON.stringify({ valid: false, error: "Invalid token" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const body = await req.json();
    const { session_token, device_id } = body;

    if (!session_token || !device_id) {
      return new Response(
        JSON.stringify({ valid: false, error: "Missing fields" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Fast check: is the session token valid and not expired?
    const { data, error: queryError } = await supabase
      .from("device_sessions")
      .select("*")
      .eq("user_id", user.id)
      .eq("device_id", device_id)
      .eq("session_token", session_token)
      .gt("expires_at", new Date().toISOString())
      .single();

    if (queryError || !data) {
      return new Response(
        JSON.stringify({ valid: false, error: "Session expired or invalid" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Update last verified
    await supabase
      .from("device_sessions")
      .update({ last_verified_at: new Date().toISOString() })
      .eq("user_id", user.id)
      .eq("device_id", device_id);

    return new Response(
      JSON.stringify({ valid: true, user_id: user.id }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err) {
    return new Response(
      JSON.stringify({ valid: false, error: "Server error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
