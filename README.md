# Anime Toolbox Android

Aplicativo Android que reúne seis ferramentas HTML em uma única Toolbox:

1. Agenda Semanal de Animes
2. Calculadora de Prazo de Animes
3. Context Dumper Pro v4.2
4. Gerador de Lista v2.0
5. Organizador de Links de Animes
6. Gerador de Posts AniKing

## Integrações Android

- Tela inicial com acesso às seis ferramentas.
- WebView com conteúdo local empacotado no APK.
- `localStorage` preservado entre usos.
- Seletor de arquivos Android para o Context Dumper e importações JSON.
- Downloads de TXT/JSON salvos em `Downloads/Anime Toolbox` no Android 10+.
- Compartilhamento nativo do Android.
- Cópia para a área de transferência.
- Notificações nativas para a Agenda Semanal.
- JSZip 3.10.1 incluído localmente para o Context Dumper funcionar sem CDN.

## Build

### GitHub Actions

O projeto já contém `.github/workflows/build-apk.yml`.

1. Coloque o projeto em um repositório GitHub.
2. Abra **Actions > Build APK > Run workflow**.
3. Ao finalizar, baixe o artifact **AnimeToolbox-debug-apk**.
4. Dentro dele estará `app-debug.apk`.

### Android Studio

Abra a pasta do projeto no Android Studio 2026.x e execute **Build > Build APK(s)**.

## Observação

A versão atual usa AGP 9.3.0, Gradle 9.5.0, compileSdk 37, Java 17 e Build Tools 36.0.0.
