package link.portalbox.pluginportal.type.language

import java.net.URL

/*
en_US	English
ar_AE	Arabic
zh_CN	Chinese - Simplified
zh_TW	Chinese - Traditional
cs_CZ	Czech
da_DK	Danish
in_ID	Indonesian
ms_MY	Malaysian
nl_NL	Dutch
fr_FR	French
fi_FI	Finnish
de_DE	German
it_IT	Italian
ja_JP	Japanese
ko_KR	Korean
no_NO	Norwegian
pl_PL	Polish
pt_BR	Portuguese
ro_RO	Romanian
ru_RU	Russian
es_ES	Spanish
sv_SE	Swedish
th_TH	Thai
tl_PH	Filipino
tr_TR	Turkish

Source: https://learn.microsoft.com/en-us/linkedin/shared/references/reference-tables/language-codes
 */

enum class Language(val supported: Boolean) {
    EN_US(true), // ENGLISH
    AR_AE(false), // Arabic
    ZH_CN(false), // Chinese (Simplified)
    ZH_TW(false), // Chinese (Traditional)
    CS_CZ(false), // Czech
    DA_DK(false), // Danish
    IN_ID(false), // Indonesian
    MS_MY(false), // Malaysian
    NL_NL(false), // Dutch
    FR_FR(false), // French
    FI_FI(false), // Finnish
    DE_DE(false), // German
    IT_IT(false), // Italian
    JA_JP(false), // Japanese
    KO_KR(false), // Korean
    NO_NO(false), // Norwegian
    PL_PL(false), // Polish
    PT_BR(false), // Portuguese
    RO_RO(false), // Romanian
    RU_RU(false), // Russian
    ES_ES(false), // Spanish
    SV_SE(false), // Swedish
    TH_TH(false), // Thai
    TL_PH(false), // Filipino
    TR_TR(false); // Turkish

}