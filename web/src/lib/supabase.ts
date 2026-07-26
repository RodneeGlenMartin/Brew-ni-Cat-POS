import { createClient } from '@supabase/supabase-js';

// Same project + publishable key the Android app ships (see DatabaseSeeder.kt). Both are public
// by nature — NEXT_PUBLIC_* values get inlined into the client bundle either way — and row-level
// security is what actually protects the data: the publishable key on its own reads nothing but
// `app_release`; every other table requires the signed-in store account.
// Env vars still win, so a staging deploy can point elsewhere without a code change.
const DEFAULT_SUPABASE_URL = 'https://hyeotyohpdpmmvquotnd.supabase.co';
const DEFAULT_SUPABASE_ANON_KEY = 'sb_publishable_orak9Nk7HGB_qFHgXMdIzA_11T8NfYQ';

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || DEFAULT_SUPABASE_URL;
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || DEFAULT_SUPABASE_ANON_KEY;

// Prevent build-time crashes if the values are ever blanked out deliberately.
export const supabase = supabaseUrl && supabaseAnonKey
  ? createClient(supabaseUrl, supabaseAnonKey)
  : (null as any);
