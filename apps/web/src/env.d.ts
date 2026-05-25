/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'spark-md5' {
  const SparkMD5: {
    ArrayBuffer: {
      new (): {
        append(data: ArrayBuffer): void
        end(): string
      }
    }
  }
  export default SparkMD5
}

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
