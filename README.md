# Calendário Anão — protótipo Android

## Gerar APK pelo GitHub
1. Envie **o conteúdo desta pasta** à raiz do repositório (não envie a pasta externa).
2. Abra **Actions → Gerar APK Android → Run workflow**.
3. Aguarde o resultado verde. Abra a execução e baixe **Calendario-Anao-APK** em Artifacts.
4. Extraia o ZIP baixado e instale **app-debug.apk** no celular.

O APK é de depuração, adequado para testes pessoais.

## Limitações atuais
O app usa dados de meses e anos do core.json offline, mas ainda muda o dia à meia-noite, não ao nascer do Sol. Fases lunares, Dia de Durin e exibição de estações/festivais não estão implementados. Não é ainda uma reprodução fiel do site.
