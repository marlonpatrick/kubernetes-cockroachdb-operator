

- Possibilitar configurações diversas via CustomResource. Exemplos: container image, replicas, resource limits, storage class (kubernetes e S3), entre outros.

- Possibilitar deploy de um cluster no modo secure.

- onAdd, onModify: comparar a versão do customresource para identificar se o evento se refere a uma versão mais antiga ou mais nova. Caso seja mais antiga, ignorar.

- onAdd / onModify: ao detectar que não será possível fazer o deploy ou modificação no cluster, algumas opções podem ser consideradas:
	- Apenas logar a situação
	- Publicar um evento
	- Matar o processo (System.exit(-1)) para que o pod do operator entre em crash loop
	- Atualizar o status do customresource informando a situação

- Conforme documentação do CockroachDB, os parâmetros "--cache" e "--max-sql-memory" devem ser setados com valores absolutos ao invés de percentual visto que o mesmo ainda não entende os limites de recursos de um container.

- Criar opção de fazer o deploy do cockroachdb client.

- Fazer ajuste fino nas imagens docker, principalmente com relação a imagem base, padronização e segurança.

- Parametrizar ownerReferences.apiVersion

- Parametrizar --as-of="$cockroachdb_current_time" no comando de dump, inclusive de não usar essa opção.

- schema.json: 
	- O campo storage ao invés de ser string pode ser um objeto Quantity do Kubernetes.
	- O campo databases poderia ser um array
	- Adicionar uma descrição nos campos com exemplos de formatação válida

- Configurar operator para monitoramento via Prometheus.