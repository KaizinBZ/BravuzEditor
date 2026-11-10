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
