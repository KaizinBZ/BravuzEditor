# BravuzEditor

Protótipo Android voltado a arquivos de SA-MP. O código corresponde à versão experimental 0.4, ainda não validada em dispositivo.

## Estado atual
- Edição básica de arquivos APK/ZIP e texto UTF-8, com exportação de cópia.
- Cópia de APK base de alguns aplicativos instalados. Não altera o aplicativo instalado e não inclui APKs divididos.
- APKs modificados são exportados sem assinatura e **não ficam prontos para instalar**.
- Análise de conteúdo é limitada; não compila Pawn nem verifica segurança.
- O app não possui verificação de atualizações do GitHub.

Para compilar: Android SDK 35, JDK 17 e Gradle 8.7. O workflow Android gera um APK de **teste**, não uma versão oficial. Consulte LEIA-ME.txt antes de usar.

## Publicação futura
Para distribuir atualizações instaláveis é necessário testar, configurar assinatura permanente protegida e criar releases. Nunca incluir token de acesso nem chave privada no código ou no APK. Repositório privado exige um canal autorizado de distribuição: não embutir credenciais no aplicativo. Uma alteração no repositório não atualiza o aplicativo automaticamente.

## Atualizações experimentais (v0.6)
O aplicativo consulta `update.json` pela internet ao iniciar e ao tocar em Verificar atualizações. A propriedade `minimumVersionCode` define o mínimo obrigatório e `latestVersionCode` a versão mais nova. As versões devem ser criadas na página Releases deste repositório. O botão abre Releases no navegador, sem baixar ou instalar silenciosamente. Para o público acessar, o repositório precisa ser público. A verificação sem conexão não bloqueia se nunca recebeu uma exigência; se já recebeu, mantém a exigência localmente até atualizar. Isso não protege versões anteriores sem o atualizador e pode ser contornado por modificações no aplicativo.

Não aumente as versões em update.json antes de disponibilizar um APK que tenha sido testado e assinado com a mesma chave da instalação. As versões debug do Actions não são adequadas como versão oficial e a assinatura debug pode mudar entre compilações. Preserve a chave de release com segurança; não versionar ou colocar a chave no APK. Android exige confirmação da instalação pelo usuário.
