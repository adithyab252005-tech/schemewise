-- SchemeWise One-Time Data Fix
-- Run this in pgAdmin or psql to fix the admin user category issue

-- 1. Clear category from admin/system accounts (they should NOT have a caste category)
UPDATE public.users
SET category = NULL
WHERE name ILIKE '%admin%' OR name ILIKE '%system%';

-- 2. Also clear any UUID-hash-looking category values across ALL users
--    (UUID format: 8-4-4-4-12 hex chars separated by hyphens, or any value with ':')
UPDATE public.users
SET category = NULL
WHERE category IS NOT NULL
  AND (
      category ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
   OR category LIKE '%:%'
   OR category NOT IN ('General', 'OBC', 'SC', 'ST', 'Minority', 'Other', 'ALL')
  );

-- 3. Verify the result
SELECT user_id, name, email, category
FROM public.users
ORDER BY user_id;
