package org.jenkinsci.plugins.servicenow

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder
import com.datapipe.jenkins.vault.VaultAccessor
import com.datapipe.jenkins.vault.credentials.VaultAppRoleCredential
import com.datapipe.jenkins.vault.credentials.VaultCredential
import hudson.model.Item
import hudson.security.ACL
import org.jenkinsci.plugins.servicenow.model.VaultConfiguration

fun readVaultData(vaultConfiguration: VaultConfiguration, credentials: VaultCredential): Map<String, String> {
    val accessor = VaultAccessor()
    accessor.init(vaultConfiguration.url)
    accessor.auth(credentials)
    return accessor.read(vaultConfiguration.path)
}

fun findCredentials(url: String, credentialId: String, vaultConfiguration: VaultConfiguration?, project: Item): Credentials? {
    var credentials: Credentials? = null
    if (vaultConfiguration != null) {
        credentials = CredentialsMatchers.firstOrNull(
                com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                        VaultAppRoleCredential::class.java,
                        project.parent, ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(url).build()),
                CredentialsMatchers.withId(credentialId))
    }
    if (credentials == null) {
        credentials = CredentialsMatchers.firstOrNull(
                com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials::class.java,
                        project.parent, ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(url).build()),
                CredentialsMatchers.withId(credentialId))
    }
    return credentials
}

fun readCredentials(credentials: Credentials, vaultConfiguration: VaultConfiguration): org.apache.http.auth.Credentials? {
    var creds: org.apache.http.auth.Credentials? = null
    if (credentials is StandardUsernamePasswordCredentials) {
        creds = org.apache.http.auth.UsernamePasswordCredentials(credentials.username, credentials.password.plainText)
    }
    if (credentials is VaultAppRoleCredential) {
        val vaultData = readVaultData(vaultConfiguration, credentials as VaultCredential)
        creds = org.apache.http.auth.UsernamePasswordCredentials(vaultData["username"], vaultData["password"])
    }
    return creds
}

class ServiceNowPluginException(message: String) : RuntimeException(message)
