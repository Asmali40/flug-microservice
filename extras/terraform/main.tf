# Copyright (C) 2025 - present Juergen Zimmermann, Hochschule Karlsruhe
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.

# Aufruf:   terraform plan
#           terraform apply -auto-approve
#           terraform destroy -auto-approve
#           terraform init -upgrade

# https://developer.hashicorp.com/terraform
# https://developer.hashicorp.com/terraform/docs

terraform {
    # https://developer.hashicorp.com/terraform/language/providers/requirements
    required_providers {
        kubernetes = {
            source  = "hashicorp/kubernetes"
            # https://registry.terraform.io/providers/hashicorp/kubernetes
            version = "2.38.0"
        }

        helm = {
            source  = "hashicorp/helm"
            # https://registry.terraform.io/providers/hashicorp/helm
            version = "3.0.2"
        }
    }

    required_version = "1.14.0"
}

###############################################################################

variable "helm_release" {
    description = "Name fuer das Helm-Release"
    type        = string
    default     = "flug"
}

variable "helm_chart" {
    description = "Pfad zum lokalen Helm-Chart"
    type        = string
    default     = "../helm/flug"
}

variable "helm_chart_version" {
    description = "Version des Helm-Charts"
    type        = string
    default     = "2025.10.1"
}

variable "namespace" {
    description = "Namespace fuer das Deployment"
    type        = string
    default     = "acme"
}

variable "dev_values" {
    description = "Pfad zur YAML-Datei mit Werten fuer Development"
    type        = string
    default     = "dev/flug.yaml"
}

variable "timeout_app" {
    description = "Timeout fuer das Ausrollen"
    type        = number
    default     = 300 # in Sekunden (ca. 100 bei JZ)
}

###############################################################################

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs
# https://github.com/hashicorp/terraform-provider-kubernetes
provider "kubernetes" {
    # https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs#config_path-1
    config_path = "~/.kube/config"
    # https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs#config_context-1
    config_context = "docker-desktop"
    # https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs#host-1
    # https://kubernetes.io/docs/concepts/security/controlling-access
    host = "https://localhost:6443"
}

# https://registry.terraform.io/providers/hashicorp/helm/latest/docs
# https://github.com/hashicorp/terraform-provider-helm
provider "helm" {
    kubernetes = {
        # https://registry.terraform.io/providers/hashicorp/helm/latest/docs#config_path-1
        config_path = "~/.kube/config"
        # https://registry.terraform.io/providers/hashicorp/helm/latest/docs#config_context-1
        config_context = "docker-desktop"
        # https://registry.terraform.io/providers/hashicorp/helm/latest/docs#host-1
        # https://kubernetes.io/docs/concepts/security/controlling-access
        host = "https://localhost:6443"
    }
}

###############################################################################

# https://registry.terraform.io/providers/hashicorp/helm/latest/docs/resources/release#example-usage---local-chart
resource "helm_release" "flug" {
    name = var.helm_release
    chart = var.helm_chart
    version = var.helm_chart_version
    values = [file("${path.module}/${var.dev_values}")]
    namespace = var.namespace
    atomic = true
    cleanup_on_fail = true
    # helm lint
    lint = true
    timeout = var.timeout_app
}
