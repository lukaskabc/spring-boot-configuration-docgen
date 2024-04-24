[#macro description option]
[#if option.description?trim?length > 0]
    ${option.description}[#t]
    [#if option.restrictions?trim?length > 0]
${nl}[#rt]
    [/#if]
[/#if]
${option.restrictions}[#t]
[/#macro]
| Variable | Description |
| --- | --- |
[#list options as option]
| [@deprecated option /]```${option.name?no_esc}```[@asterisk option /] | [@markdown_single_line][@description option /][/@markdown_single_line] |
[/#list]

[@legend /]
