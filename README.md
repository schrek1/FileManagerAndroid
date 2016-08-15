# FileManagerAndroid
File Manager for Android

Jednoduchý souborový manager:

- aplikace bude umožňovat jednoduché procházení souborů v telefonu

- aplikace bude obsahovat Toolbar, který bude obsahovat 2 action buttons. Refresh, které obnoví

aktuální složku a settings, které umožní změnu výchozí složky. Settings by mělo být v overflow menu.

- při klepnutí na Setting se spustí nová aktivita s PreferenceFragment s jednou položkou "defaultfolder"

- při spuštění app se načte výchozí složka (v separátním vlákně) a zobrazí se jako list položek v portrait a grid v landscape.

- při klepnutí na položku složky se otevře daná složka (s libovolnou animaci)

- při klepnutí na soubor se pokusí aplikace soubor otevřít v defaultní aplikaci pro daný typ souboru

- při dlouhém stisku na položku se objeví CAB umožňující smazání vybraných souborů/složek.

Všechny vybrané položky musí být viditelně zvýrazněné.

Aplikaci vytvářejte tak, jak byste ji chtěli sami používat.
