// personne/enums/StatutAncrage.java
package bf.gov.ascelc.logintegrite_backend.personne.enums;

// Statut d'ancrage d'une personne dans le systeme.
// Calcul :
//  - REGISTRE_OFFICIEL : la personne a au moins un dossier
//    dont TOUS les faits reproches ont ete VALIDEE
//  - EN_INSTRUCTION    : aucun de ses dossiers n'est entierement valide
public enum StatutAncrage {
    EN_INSTRUCTION,
    REGISTRE_OFFICIEL
}
