# Anime Toolbox PWA v1.1.0

A Anime Toolbox agora é um site estático/PWA para GitHub Pages. Ela reúne sete ferramentas e pode ser instalada pelo navegador, funcionar em modo standalone e abrir offline após o primeiro carregamento.

## Ferramentas

1. Agenda Semanal de Animes
2. Calculadora de Prazo de Animes
3. Context Dumper Pro v4.2
4. Gerador de Lista v2.0
5. Organizador de Links de Animes
6. Gerador de Changelog
7. Markdown / BBCode Toolbox

## Publicar no GitHub Pages

1. Coloque **todo o conteúdo desta pasta na raiz do repositório**.
2. No GitHub, abra **Settings > Pages**.
3. Em **Build and deployment > Source**, escolha **Deploy from a branch**.
4. Selecione a branch **main** e a pasta **/(root)**.
5. Salve e aguarde a publicação.

O projeto usa apenas caminhos relativos, portanto funciona em URLs de projeto como:

`https://usuario.github.io/nome-do-repositorio/`

## Instalar como PWA

No Android/Chrome, abra o site publicado e use **Instalar app** / **Adicionar à tela inicial**. A página inicial também mostra o botão **Instalar** quando o navegador disponibiliza o evento de instalação.

## Offline e atualizações

O `sw.js` mantém as sete ferramentas, o JSZip e os ícones em cache. Após o primeiro carregamento online, a Toolbox pode abrir sem internet. Quando uma versão nova do service worker é detectada, a interface avisa que existe atualização disponível.

Ao publicar uma alteração importante, incremente o valor `CACHE_NAME` em `sw.js` para forçar uma nova geração do cache (por exemplo, `anime-toolbox-pwa-v1.1.1-1`).

## Dados locais

Dados armazenados com `localStorage` continuam locais ao navegador/dispositivo. Limpar os dados do site ou desinstalar a PWA pode remover esses dados; use os recursos de exportação/backup quando forem importantes.

## Estrutura

```text
AnimeToolbox/
├── index.html
├── agenda.html
├── calculadora.html
├── changelog.html
├── context_dumper.html
├── lista.html
├── markdown_bbcode.html
├── organizador.html
├── manifest.webmanifest
├── pwa.js
├── sw.js
├── toolbox_inventory.json
├── .nojekyll
├── icons/
│   ├── icon-192.png
│   ├── icon-512.png
│   └── icon-maskable-512.png
└── vendor/
    └── jszip.min.js
```

A antiga camada Android (`app/`, Gradle, AndroidManifest, MainActivity e workflow de APK) não é necessária para esta versão PWA.
