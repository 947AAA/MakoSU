/// <reference lib="webworker" />

const CACHE_NAME: string = 'makosu-v1.2'
const STATIC_CACHE: string = 'makosu-static-v1.2'
const RUNTIME_CACHE: string = 'makosu-runtime-v1.2'
const IMAGE_CACHE: string = 'makosu-images-v1.2'

const CRITICAL_ASSETS: string[] = [
  '/MakoSU/',
  '/MakoSU/guide/',
  '/MakoSU/guide/installation',
  '/MakoSU/guide/compatibility',
  '/MakoSU/guide/susfs',
  '/MakoSU/guide/troubleshooting',
  '/MakoSU/en/',
  '/MakoSU/en/guide/',
  '/MakoSU/en/guide/installation',
  '/MakoSU/en/guide/compatibility',
  '/MakoSU/en/guide/susfs',
  '/MakoSU/en/guide/troubleshooting',
  '/MakoSU/makosu-manager.png',
  '/MakoSU/favicon-96x96.png',
  '/MakoSU/offline.html',
]

const CACHE_STRATEGIES: {
  static: string[]
  images: string[]
  documents: string[]
} = {
  static: ['.css', '.js', '.woff2', '.woff', '.ttf', '.otf'],
  images: ['.png', '.jpg', '.jpeg', '.svg', '.webp', '.avif', '.gif', '.ico'],
  documents: ['.html', '.json', '.xml'],
}

self.addEventListener('install', (event: ExtendableEvent) => {
  event.waitUntil(
    (async (): Promise<void> => {
      const cache: Cache = await caches.open(STATIC_CACHE)
      try {
        await cache.addAll(CRITICAL_ASSETS)
      } catch (error) {
        console.warn('Failed to cache some assets:', error)
      }
      ;(self as any).skipWaiting()
    })()
  )
})

self.addEventListener('activate', (event: ExtendableEvent) => {
  event.waitUntil(
    (async (): Promise<void> => {
      const cacheNames: string[] = await caches.keys()
      const validCaches: string[] = [STATIC_CACHE, RUNTIME_CACHE, IMAGE_CACHE, CACHE_NAME]

      await Promise.all(
        cacheNames
          .filter((name: string) => !validCaches.includes(name))
          .map((name: string) => caches.delete(name))
      )
      ;(self as any).clients.claim()
    })()
  )
})

self.addEventListener('fetch', (event: FetchEvent) => {
  const { request } = event
  const url: URL = new URL(request.url)

  if (request.method !== 'GET' || url.origin !== location.origin) return
  if (url.pathname.startsWith('/api/')) return

  event.respondWith(handleRequest(request, url))
})

async function handleRequest(request: Request, url: URL): Promise<Response> {
  const isStatic: boolean = CACHE_STRATEGIES.static.some((ext: string) =>
    url.pathname.endsWith(ext)
  )
  const isImage: boolean = CACHE_STRATEGIES.images.some((ext: string) => url.pathname.endsWith(ext))
  const isDocument: boolean =
    CACHE_STRATEGIES.documents.some((ext: string) => url.pathname.endsWith(ext)) ||
    url.pathname.endsWith('/')

  try {
    if (isStatic) {
      return await handleStatic(request)
    } else if (isImage) {
      return await handleImage(request)
    } else if (isDocument) {
      return await handleDocument(request)
    } else {
      return await handleDefault(request)
    }
  } catch (error) {
    return await handleOffline(request)
  }
}

async function handleStatic(request: Request): Promise<Response> {
  const cache: Cache = await caches.open(STATIC_CACHE)
  const cached: Response | undefined = await cache.match(request)

  if (cached) {
    fetch(request)
      .then((response: Response) => {
        if (response.ok) cache.put(request, response.clone())
      })
      .catch(() => {})

    return cached
  }

  const response: Response = await fetch(request)
  if (response.ok) {
    cache.put(request, response.clone())
  }
  return response
}

async function handleImage(request: Request): Promise<Response> {
  const cache: Cache = await caches.open(IMAGE_CACHE)
  const cached: Response | undefined = await cache.match(request)

  if (cached) return cached

  const response: Response = await fetch(request)
  if (response.ok) {
    cache.put(request, response.clone())
  }
  return response
}

async function handleDocument(request: Request): Promise<Response> {
  const cache: Cache = await caches.open(RUNTIME_CACHE)

  try {
    const response: Response = await fetch(request)
    if (response.ok) {
      cache.put(request, response.clone())
    }
    return response
  } catch (error) {
    const cached: Response | undefined = await cache.match(request)
    if (cached) return cached

    if (request.mode === 'navigate') {
      const offlinePage: Response | undefined = await caches.match('/MakoSU/offline.html')
      if (offlinePage) return offlinePage
    }

    throw error
  }
}

async function handleDefault(request: Request): Promise<Response> {
  const cache: Cache = await caches.open(RUNTIME_CACHE)

  try {
    const response: Response = await fetch(request)
    if (response.ok) {
      cache.put(request, response.clone())
    }
    return response
  } catch (error) {
    const cached: Response | undefined = await cache.match(request)
    if (cached) return cached
    throw error
  }
}

async function handleOffline(request: Request): Promise<Response> {
  const cache: Cache = await caches.open(STATIC_CACHE)

  if (request.mode === 'navigate') {
    const offlinePage: Response | undefined = await caches.match('/MakoSU/offline.html')
    if (offlinePage) return offlinePage
  }

  return new Response('Offline', {
    status: 503,
    statusText: 'Service Unavailable',
    headers: new Headers({
      'Content-Type': 'text/plain',
    }),
  })
}

self.addEventListener('sync', (event: any) => {
  if (event.tag === 'data-sync') {
    event.waitUntil(syncData())
  }
})

async function syncData(): Promise<void> {
  try {
    console.log('Syncing application data...')
  } catch (error) {
    console.error('Data sync failed:', error)
  }
}

interface PerformanceMessage {
  type: string
  name: string
}

self.addEventListener('message', (event: MessageEvent) => {
  const data = event.data as PerformanceMessage
  if (data && data.type === 'PERFORMANCE_MARK') {
    performance.mark(data.name)
  }
})
