// Copyright (C) 2025 - present Juergen Zimmermann, Hochschule Karlsruhe
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

import { Chart } from '@pulumi/kubernetes/helm/v4/chart.js';
import { Config } from '@pulumi/pulumi/config.js';
import { FileAsset } from '@pulumi/pulumi/asset/asset.js';
import { Provider } from '@pulumi/kubernetes/provider.js';
import { readFile } from 'node:fs/promises';
import { resolve } from 'node:path';

// ============================================================================

const chartName = 'flug';
// https://nodejs.org/api/path.html
const chart = resolve('..', 'helm', 'flug');
const version = '2025.10.2';
const config = new Config();
const namespace = config.get('k8sNamespace') || 'default';
const devYamlFile = resolve('dev', 'flug.yaml');
const ENV_HOME = 'USERPROFILE';
// macOS:
// const ENV_HOME = 'HOME';
const provider = new Provider('docker-desktop', {
    // https://nodejs.org/api/fs.html#fspromisesreadfilepath-options
    // "await" wie in C#
    kubeconfig: await readFile(resolve(process.env[ENV_HOME]!, '.kube', 'config'), 'utf8'),
});

// ============================================================================

// Mit einem  Helm-Chart neue Kubernetes-Ressourcen erstellen:
// v4 nutzt nicht "Helm CLI", sondern v4 entspricht: kubectl + "Helm Templating"
// https://www.pulumi.com/registry/packages/kubernetes/api-docs/helm/v4/chart
// https://www.pulumi.com/blog/kubernetes-chart-v4
new Chart(chartName, {
    chart,
    version,
    namespace,
    valueYamlFiles: [new FileAsset(devYamlFile)],
}, {
    provider
});
