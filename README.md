
# Autorização

## AWS Lambda
LambdaAuthorizer é a função lambda que faz a autorização.
Ela recebe um request APIGatewayProxyRequestEvent, e retorna AuthorizerResponse
Através da integração com o APIGateway acessando o JWT verifica se ele é válido.

### Tipo de cliente
Há três possibiliades:
- JWT de usuário do tipo Administrador
- JWT de usuário cliente que informou o cpf
- JWT de usuário cliente que não informou o cpf, anônimo

