## Requirements

The following requirements are needed by this module:

- <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) (1.14.0)

- <a name="requirement_helm"></a> [helm](#requirement\_helm) (3.0.2)

- <a name="requirement_kubernetes"></a> [kubernetes](#requirement\_kubernetes) (2.38.0)

## Providers

The following providers are used by this module:

- <a name="provider_helm"></a> [helm](#provider\_helm) (3.0.2)

## Modules

No modules.

## Resources

The following resources are used by this module:

- [helm_release.flug](https://registry.terraform.io/providers/hashicorp/helm/3.0.2/docs/resources/release) (resource)

## Required Inputs

No required inputs.

## Optional Inputs

The following input variables are optional (have default values):

### <a name="input_dev_values"></a> [dev\_values](#input\_dev\_values)

Description: Pfad zur YAML-Datei mit Werten fuer Development

Type: `string`

Default: `"dev/flug.yaml"`

### <a name="input_helm_chart"></a> [helm\_chart](#input\_helm\_chart)

Description: Pfad zum lokalen Helm-Chart

Type: `string`

Default: `"../helm/flug"`

### <a name="input_helm_chart_version"></a> [helm\_chart\_version](#input\_helm\_chart\_version)

Description: Version des Helm-Charts

Type: `string`

Default: `"2025.10.1"`

### <a name="input_helm_release"></a> [helm\_release](#input\_helm\_release)

Description: Name fuer das Helm-Release

Type: `string`

Default: `"flug"`

### <a name="input_namespace"></a> [namespace](#input\_namespace)

Description: Namespace fuer das Deployment

Type: `string`

Default: `"acme"`

### <a name="input_timeout_app"></a> [timeout\_app](#input\_timeout\_app)

Description: Timeout fuer das Ausrollen

Type: `number`

Default: `300`

## Outputs

No outputs.