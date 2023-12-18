let defaultEditorContent =  {
    "fr":"$$span\n" +
        "<div class='h-100 p-5 rounded-3 h-100 p-5 text-bg-dark rounded-3 bg-dark'>\n" +
        "<h2 class='text-white'> Bienvenue dans le powered by Lutece</h2>\n" +
        "<p class='text-light'>Cet outil se veut être un outil collaboratif très simple pour votre site Lutece</p>\n" +
        "<a href='https://lutece.paris.fr/fr/' class='btn btn-outline-light text-white' style='text-decoration: none'>Learn more</a>\n" +
        "</div>\n" +
        "$$"+
        "\n" +
        "# Fonctionnalités principales\n" +
        "\n" +
        "\n" +
        "* Utilisation de la syntaxe standard **Markdown** \n" +
        "\n" +
        " Possibilité de personnaliser directement en html pour obtenir des rendus spécifiques. Les éléments html autorisés sont : les classes de Lutece ou de bootstrap, des paragraphes, les Headings, les ancres, les images et les div, figure, figcaption. Toujours à l'intérieur des balises custom : \n" +
        "\n" +
        "$$span\n" +
        "<span style='display: flex;justify-content: space-between; '><p>My text is on my left and the image on my right</p><figure><img src='image?resource_type=wiki_image&id=1' alt='LUTECE logo' title='LUTECE logo' class='' width='200' height='' align=''><figcaption>LUTECE logo</figcaption></figure></span>\n" +
        "$$"+
        "\n" +
        "- [x] Entièrement intégré à la plate-forme Lutece :\n" +
        "- [x] Authentification MyLutece et gestion des rôles\n" +
        "- [x]  Support des avatars et des pseudos Lutece\n" +
        "\n" +
        "## Quelques exemples de rendu graphique\n" +
        "\n" +
        "*Vous pouvez cliquer sur le bouton Edition pour voir le code source Wiki de tous ces exemples. Toute la syntaxe est disponible via le bouton d'aide situé en haut à droite.*\n" +
        "\n" +
        "\n" +
        "$$span\n" +
        "<p>De nombreux types de label peuvent être créés, ex : <span class='badge badge-badge bg-info'>Note</span> <span class='badge badge-badge bg-warning'>Caution</span> <span class='badge badge-badge bg-success'>Awesome</span> ... et aussi des badges : <span class='badge'>256</span></p>\n" +
        "$$"+
        "\n" +
        "\n" +
        "## Color Syntax Plugin\n" +
        "\n" +
        "<span style='color:#86c1b9'>Click the color picker button on the toolbar!</span>\n" +
        "\n" +
        "## Table Merged Cell Plugin\n" +
        "\n" +
        "| @cols=2:merged |\n" +
        "| --- | --- |\n" +
        "| table | table2 |\n" +
        "\n" +
        "\n" +
        "\n" +
        "## Code Syntax Highlighting Plugin\n" +
        "\n" +
        "```javascript\n" +
        "console.log('foo')\n" +
        "```\n" +
        "```html\n" +
        "<div id='editor'><span>baz</span></div>\n" +
        "```\n" +
        "\n" +
        "$$span\n" +
        "<div class='alert alert-info'> <span class='ti ti-info-circle'></span> This is an info alert </div>\n" +
        "$$"+
        "\n" +
        "\n" +
        "$$span\n" +

        "<div class='alert alert-danger'> <span class='ti ti-exclamation-triangle'></span> This is an warning alert </div>\n" +
        "$$\n" +
        "\n" +
        "## Une table des matières :\n" +
        "Si l'input Table des matière (TOC) est présent dans le texte, une table des matières est crée automatiquement lors de la publication à partir des éléments H1, H2 et H3. \n" +
        "\n" +
        "$$span\n" +
        "<span class='toc'></span>\n" +
        "$$"+
        "\n" +
        "## Un Dark mode\n" +
        "Si l'input 'Dark mode' est présent dans le texte, un bouton permettant d'activité le dark mode apparaît en haut à droite.\n" +
        "\n" +
        "$$span\n" +
        "<span class='darkModeClassOn'></span>\n" +
        "$$"+
        "\n" +
        "## Intégration d'Iframe\n" +
        "<iframe width='1200' height='720' src='https://www.youtube.com/embed/fWrI1NYG6Eo' title='Lutece Platform' frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' allowfullscreen></iframe>\n" +
        "\n" +
        "\n" +
        "## Intégration de Latex.js\n" +
        "\n" +
        "$$latex\n" +
        "\\documentclass{article}\n" +
        "\\begin{document}\n" +
        "\n" +
        "$\n" +
        "f(x) = \\int_{-\\infty}^\\infty \\hat f(\\xi),e^{2 \\pi i \\xi x} , d\\xi\n" +
        "$\n" +
        "\\end{document}\n" +
        "$$"+
        "\n",
    "en":
        "$$span\n" +
        "<div class='h-100 p-5 rounded-3 h-100 p-5 text-bg-dark rounded-3 bg-dark'>\n" +
        "<h2 class='text-white'> Welcome to the Wiki powered by Lutece</h2>\n" +
        "<p class='text-light'>This Wiki aims to be a very simple collaborative tool fully integrated to your Lutece site</p>\n" +
        "<a href='https://lutece.paris.fr/fr/' class='btn btn-outline-light text-white' style='text-decoration: none'>Learn more</a>\n" +
        "</div>\n" +
        "$$"+
        "\n" +
        "# Key features\n" +
        "\n" +
        "- [x] Provide a very simple and efficient Wiki editor with removable Help panel\n" +
        "- [x] Fully integrated to Lutece platform :\n" +
        "    - [x] Use standard **Markdown** \n" +
        "- [x] Can be easily customized with the possiblity to add some html. Allowed html elements are : classes from Lutece, bootstrap or your own, paragraphs, Headings, ancres, images, div, figure, figcaption. Alway inside a custom input :\n" +
        "\n" +
        "$$span\n" +
        "<span style='display: flex;justify-content: space-between; '><p>My text is on my left and the image on my right</p><figure><img src='image?resource_type=wiki_image&id=1' alt='LUTECE logo' title='LUTECE logo' class='' width='200' height='' align=''><figcaption>LUTECE logo</figcaption></figure></span>\n" +
        "$$"+
        "\n" +
        "- [x] Fully integrated in Lutece\n" +
        "- [x] Authentication MyLutece et role RBAC\n" +
        "- [x]  Support Lutece's avatar and pseudo features\n" +
        "\n" +
        "## Quick rendering samples\n" +
        "\n" +
        "Just click on Edit button to see the code of all this samples. All the syntax is available clicking on the Help button located at the top right corner.\n" +
        "$$span\n" +
        "<p>Various labels can be created, ex : <span class='badge badge-badge bg-info'>Note</span> <span class='badge badge-badge bg-warning'>Caution</span> <span class='badge badge-badge bg-success'>Awesome</span> .\n" +
        "$$"+
        "\n" +
        "## Color Syntax Plugin\n" +
        "\n" +
        "<span style='color:#86c1b9'>Click the color picker button on the toolbar!</span>\n" +
        "\n" +
        "## Table Merged Cell Plugin\n" +
        "\n" +
        "| @cols=2:merged |\n" +
        "| --- | --- |\n" +
        "| table | table2 |\n" +
        "\n" +
        "\n" +
        "## Code Syntax Highlighting Plugin\n" +
        "\n" +
        "```javascript\n" +
        "console.log('foo')\n" +
        "```\n" +
        "```html\n" +
        "<div id='editor'><span>baz</span></div>\n" +
        "```\n" +
        "\n" +
        "$$span\n" +
        "<div class='alert alert-info'> <span class='ti ti-info-circle'></span> This is an info alert </div>\n" +
        "$$"+
        "\n" +
        "$$span\n" +
        "<div class='alert alert-danger'> <span class='ti ti-exclamation-triangle'></span> This is an warning alert </div>\n" +
        "$$ \n" +
        "\n" +
        "## A table of contents :\n" +
        "If you have the 'Table of Content' input (TOC) in the content of your text, you will a menu after publishing you topic version, based on the headings H1, H2 and H3.\n" +
        "\n" +
        "$$span\n" +
        "<span class='toc'></span>\n" +
        "$$"+
        "\n" +
        "## A Dark mode\n" +
        "There an input witch allow you the activate a dark mode feature.\n" +
        "\n" +
        "$$span\n" +
        "<span class='darkModeClassOn'></span>\n" +
        "$$"+
        "\n" +
        "## Intégration d'Iframe\n" +
        "<iframe width='1200' height='720' src='https://www.youtube.com/embed/fWrI1NYG6Eo' title='Lutece Platform' frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' allowfullscreen></iframe>\n" +
        "\n" +
        "\n" +
        "## Intégration de Latex.js\n" +
        "\n" +
        "$$latex\n" +
        "\\documentclass{article}\n" +
        "\\begin{document}\n" +
        "\n" +
        "$\n" +
        "f(x) = \\int_{-\\infty}^\\infty \\hat f(\\xi),e^{2 \\pi i \\xi x} , d\\xi\n" +
        "$\n" +
        "\\end{document}\n" +
        "$$"+
        "\n" +
        "\n",
};