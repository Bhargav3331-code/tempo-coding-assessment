1. Expired entries are never removed (unbounded memory growth) - Entries that exceed their TTL are treated as invalid by get, but they are never evicted from the map.
**Impact in production**
i) The cache grows indefinitely as new keys are added.
ii) Long-running services will experience memory leaks.
iii) Increased GC pressure and eventual OutOfMemoryError.
iv) size() becomes misleading and useless as a signal.

2. Race condition between expiration check and usage (stale reads) - Expiration is checked outside any atomic operation.
**Impact in production**
i) Clients may observe stale data.
ii) Violates cache freshness expectations
iii) Particularly problematic for correctness-sensitive data (auth, pricing, configuration)

3. Time source is unsafe for expiration logic - Using System.currentTimeMillis() for TTL tracking
**Impact in production**
i) Entries may:
   Never expire
   Expire immediately
ii) Hard-to-debug time-related inconsistencies

4. No visibility or configurability of TTL
**Impact in production**
i) Forces redeployments to change cache behaviour
ii) Encourages misuse or copy-paste implementations

5. Cache size() is misleading and unsafe for monitoring
**Impact in production**
i) Operational dashboards show incorrect cache size
ii) Can mislead autoscaling and alerting decisions

6. No removal of expired entries on access
**Impact in production**
i) Same expired entry is rechecked repeatedly
ii) Increased contention under high read load

7. No eviction strategy under memory pressure
**Impact in production**
i) High-cardinality keys → runaway memory usage
ii) GC pressure and latency spikes
iii) Cache becomes a liability rather than a performance optimization

 
