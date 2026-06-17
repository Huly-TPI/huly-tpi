import test from 'node:test'
import assert from 'node:assert/strict'
import { mkdtemp, readFile, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'
import ts from 'typescript'

const loadRuntimeConfigModule = async () => {
  const sourcePath = path.resolve('src/utils/runtimeConfig.ts')
  const source = await readFile(sourcePath, 'utf8')
  const transpiled = ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.ESNext,
      target: ts.ScriptTarget.ES2020,
    },
    fileName: sourcePath,
  })

  const tempDir = await mkdtemp(path.join(tmpdir(), 'huly-antiscroll-tests-'))
  const tempFile = path.join(tempDir, 'runtimeConfig.mjs')
  await writeFile(tempFile, transpiled.outputText, 'utf8')

  try {
    return await import(`file://${tempFile}`)
  } finally {
    await rm(tempDir, { recursive: true, force: true })
  }
}

const runtimeConfig = await loadRuntimeConfigModule()

test('trimTrailingSlash removes trailing slashes', () => {
  assert.equal(
    runtimeConfig.trimTrailingSlash('https://huly-tpi.onrender.com///'),
    'https://huly-tpi.onrender.com',
  )
})

test('buildAppOrigins returns unique origins in stable order', () => {
  assert.deepEqual(
    runtimeConfig.buildAppOrigins(
      'https://huly-tpi-frontend.onrender.com',
      ['https://huly-tpi-frontend.onrender.com', 'http://localhost:5173'],
    ),
    ['https://huly-tpi-frontend.onrender.com', 'http://localhost:5173'],
  )
})

test('resolveExtensionEnvironment uses known extension ids', () => {
  assert.equal(
    runtimeConfig.resolveExtensionEnvironment('opafcmdcpkhcfnbfmfipdninpkkpamgh', false),
    'prod',
  )
  assert.equal(
    runtimeConfig.resolveExtensionEnvironment('kmkblhmalfkbnkipaohgfllbecajkpom', true),
    'local',
  )
  assert.equal(runtimeConfig.resolveExtensionEnvironment(undefined, true), 'prod')
  assert.equal(runtimeConfig.resolveExtensionEnvironment(undefined, false), 'local')
})

test('normalizeLegacyUrls migrates legacy localhost values', () => {
  const migrated = runtimeConfig.normalizeLegacyUrls(
    {
      enabled: true,
      pauseIntervalSeconds: 1200,
      gardenUrl: 'http://localhost:5173/garden',
      backendUrl: 'http://localhost:8080',
      monitoredDomains: ['facebook.com'],
      dataSharingConsent: false,
    },
    {
      backendUrl: 'https://huly-tpi.onrender.com',
      gardenUrl: 'https://huly-tpi-frontend.onrender.com/garden',
    },
    'http://localhost:5173',
    'http://localhost:8080',
  )

  assert.equal(migrated.backendUrl, 'https://huly-tpi.onrender.com')
  assert.equal(migrated.gardenUrl, 'https://huly-tpi-frontend.onrender.com/garden')
})

test('normalizeLegacyUrls preserves non-legacy values', () => {
  const migrated = runtimeConfig.normalizeLegacyUrls(
    {
      enabled: true,
      pauseIntervalSeconds: 1200,
      gardenUrl: 'https://app.example.com/garden',
      backendUrl: 'https://api.example.com',
      monitoredDomains: ['facebook.com'],
      dataSharingConsent: false,
    },
    {
      backendUrl: 'https://huly-tpi.onrender.com',
      gardenUrl: 'https://huly-tpi-frontend.onrender.com/garden',
    },
    'http://localhost:5173',
    'http://localhost:8080',
  )

  assert.equal(migrated.backendUrl, 'https://api.example.com')
  assert.equal(migrated.gardenUrl, 'https://app.example.com/garden')
})

test('buildRuntimeConfigPatch only returns changed values', () => {
  assert.deepEqual(
    runtimeConfig.buildRuntimeConfigPatch(
      {
        backendUrl: 'https://old-api.example.com',
        gardenUrl: 'https://old-app.example.com/garden',
      },
      {
        backendUrl: 'https://new-api.example.com',
      },
    ),
    {
      backendUrl: 'https://new-api.example.com',
    },
  )
})

test('isInjectableHttpTab only accepts http or https tabs with numeric id', () => {
  assert.equal(runtimeConfig.isInjectableHttpTab({ id: 1, url: 'https://facebook.com' }), true)
  assert.equal(runtimeConfig.isInjectableHttpTab({ id: 2, url: 'http://localhost:5173' }), true)
  assert.equal(runtimeConfig.isInjectableHttpTab({ id: 3, url: 'chrome://extensions' }), false)
  assert.equal(runtimeConfig.isInjectableHttpTab({ url: 'https://facebook.com' }), false)
})
