import NodeCache from 'node-cache';
    
class CacheService {
  private static instance: CacheService;
  private cache: NodeCache;
  private ttl: number = 60*10;

  private constructor() {
    this.cache = new NodeCache();
  }

  public static getInstance(): CacheService {
    if (!CacheService.instance) {
      CacheService.instance = new CacheService();
    }
    return CacheService.instance;
  }

  public get<T>(key: string): T | undefined {
    return this.cache.get<T>(key);
  }

  public set<T>(key: string, value: T, ttl: number): boolean {
    return this.cache.set<T>(key, value, ttl);
  }

  public del(key: string): number {
      return this.cache.del(key);
  }
  public resetTTL(){
    this.cache.keys().forEach( key => {
        this.cache.ttl(key,this.ttl)
    });
  }
}

export const cacheService = CacheService.getInstance();