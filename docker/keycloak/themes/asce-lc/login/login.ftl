<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<#import "social-providers.ftl" as identityProviders>
<#import "passkeys.ftl" as passkeys>
<#-- Après une demande de "mot de passe oublié", Keycloak revient sur cette
     page de login avec un message de type "success" : on affiche alors un
     état "vérifiez votre boîte mail" dédié plutôt que ce message collé
     au-dessus du formulaire de connexion habituel (source de confusion : on
     ne sait plus s'il faut attendre l'email ou retaper un mot de passe). -->
<#assign asceResetEmailSent = message?? && message.type?? && message.type == "success">
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') && !asceResetEmailSent displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
<!-- template: login.ftl -->

    <#if section = "header">
        <#if asceResetEmailSent>${msg("emailForgotTitle")!"Vérifiez votre boîte mail"}<#else>${msg("loginAccountTitle")}</#if>
    <#elseif section = "form">
        <#if asceResetEmailSent>
        <div class="asce-reset-sent">
            <div class="asce-reset-sent-icon"><i class="fas fa-envelope" aria-hidden="true"></i></div>
            <p class="asce-login-subtitle">${kcSanitize(message.summary)?no_esc}</p>
            <a class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
               href="${url.loginRestartFlowUrl}">${msg("backToLogin")!"Retour à la connexion"}</a>
        </div>
        <#else>
        <div class="asce-login-heading">
            <h2 class="asce-login-title">${msg("loginAccountTitle")}</h2>
            <p class="asce-login-subtitle">${msg("loginPageSubtitle")}</p>
        </div>
        <div id="kc-form">
          <div id="kc-form-wrapper">
            <#if realm.password>
                <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post" novalidate="novalidate">
                    <#if !usernameHidden??>
                        <#assign label>
                            <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
                        </#assign>
                        <@field.input name="username" label=label required=true error=kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc
                            autofocus=true autocomplete="${(enableWebAuthnConditionalUI?has_content)?then('username webauthn', 'username')}" value=login.username!'' />
                        <@field.password name="password" label=msg("password") required=true error="" forgotPassword=realm.resetPasswordAllowed autofocus=usernameHidden?? autocomplete="current-password">
                            <#if realm.rememberMe && !usernameHidden??>
                                <@field.checkbox name="rememberMe" label=msg("rememberMe") value=login.rememberMe?? />
                            </#if>
                        </@field.password>
                    <#else>
                        <@field.password name="password" label=msg("password") required=true forgotPassword=realm.resetPasswordAllowed autofocus=usernameHidden?? autocomplete="current-password">
                            <#if realm.rememberMe && !usernameHidden??>
                                <@field.checkbox name="rememberMe" label=msg("rememberMe") value=login.rememberMe?? />
                            </#if>
                        </@field.password>
                    </#if>

                    <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                    <@buttons.loginButton />
                </form>
            </#if>
            </div>
        </div>
        <@passkeys.conditionalUIData />
        </#if>
    <#elseif section = "socialProviders" >
        <#if !asceResetEmailSent && realm.password && social.providers?? && social.providers?has_content>
            <@identityProviders.show social=social/>
        </#if>
    <#elseif section = "info" >
        <#if !asceResetEmailSent && realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
