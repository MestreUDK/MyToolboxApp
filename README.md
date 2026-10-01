# Anime Toolbox Android

Aplicativo Android que reúne sete ferramentas HTML em uma única Toolbox:

1. Agenda Semanal de Animes
2. Calculadora de Prazo de Animes
3. Context Dumper Pro v4.2
4. Gerador de Lista v2.0
5. Organizador de Links de Animes
6. Gerador de Changelog
7. Markdown / BBCode Toolbox

## Integrações Android

- Tela inicial com acesso às sete ferramentas.
- WebView com conteúdo local empacotado no APK.
- `localStorage` preservado entre usos.
- Seletor de arquivos Android para o Context Dumper e importações JSON.
- Downloads de TXT/JSON salvos em `Downloads/Anime Toolbox` no Android 10+.
- Compartilhamento nativo do Android.
- Cópia para a área de transferência.
- Notificações nativas para a Agenda Semanal.
- JSZip 3.10.1 incluído localmente para o Context Dumper funcionar sem CDN.

## Ferramentas novas

### Gerador de Changelog

Controle SemVer, itens de atualização, modo manutenção, mensagem pronta para Telegram/Discord e geração de `version.json` e `manutencao.json`.

### Markdown / BBCode Toolbox

Editor offline com atalhos de formatação, prévia e conversão entre Markdown, BBCode e texto simples. Inclui títulos, links, citações, código, spoilers, listas e tabelas.

## Build

### GitHub Actions

O projeto utiliza `.github/workflows/build-apk.yml`.

1. Coloque o projeto em um repositório GitHub.
2. Abra **Actions > Build APK > Run workflow**.
3. Ao finalizar, baixe o artifact **AnimeToolbox-debug-apk**.
4. Dentro dele estará `app-debug.apk`.
