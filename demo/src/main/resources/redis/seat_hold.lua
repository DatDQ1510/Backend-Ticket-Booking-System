-- Atomic seat hold (all-or-nothing)
-- KEYS: seat hold keys (e.g. seat:hold:{eventId}:{seatId})
-- ARGV[1]: ttlMillis (number)
-- ARGV[2]: holdToken (string)
-- Return: 1 if success, 0 if any seat already held

for i = 1, #KEYS do
  if redis.call('EXISTS', KEYS[i]) == 1 then
    return 0
  end
end

for i = 1, #KEYS do
  redis.call('PSETEX', KEYS[i], ARGV[1], ARGV[2])
end

return 1
