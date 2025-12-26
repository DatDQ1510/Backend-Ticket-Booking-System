-- Release seat holds
-- KEYS: seat hold keys
-- ARGV[1]: holdToken (string) - only delete if value matches
-- Return: number of keys released

local released = 0

for i = 1, #KEYS do
  local current = redis.call('GET', KEYS[i])
  if current == ARGV[1] then
    redis.call('DEL', KEYS[i])
    released = released + 1
  end
end

return released
