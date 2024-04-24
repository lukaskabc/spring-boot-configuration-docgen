<h1>This configuration output belongs to <a href="https://github.com/kbss-cvut/termit">TermIt</a> and serves only as the example of output format (file has been truncated).</h1>

<table>
    <tr>
        <th>Variable</th>
        <th>Description</th>
    </tr>
    <tr>
        <td><code>APPLICATION_VERSION</code></td>
        <td>
            Default value: <code>development</code>
        </td>
    </tr>
    <tr>
        <td><code>SPRING_MAIL_USERNAME</code></td>
        <td>
            Default value: <code>#{null}</code>
        </td>
    </tr>
    <tr>
        <td><code>TERMIT_ACL_DEFAULTEDITORACCESSLEVEL</code></td>
        <td>
            Default access level for users in editor role.
            <br>
            Default value: <code>READ</code>
        </td>
    </tr>
    <tr>
        <td><code>TERMIT_CHANGETRACKING_CONTEXT_EXTENSION</code><b>&#42;</b></td>
        <td>
            Extension appended to asset identifier (presumably a vocabulary ID) to denote its change tracking context
            identifier.
            <br>
            value must be present
        </td>
    </tr>
    <tr>
        <td><i>Deprecated </i><code>TERMIT_TEXTANALYSIS_TERMASSIGNMENTMINSCORE</code><b>&#42;</b></td>
        <td>
            Minimal match score of a term occurrence for which a term assignment should be automatically generated.<p>
            More specifically, when annotated file content is being processed, term occurrences with sufficient score
            will cause creation of corresponding term assignments to the file.<br>
            Deprecated: This configuration is currently not used.
            <br>
            value must be present
        </td>
    </tr>
    <tr>
        <td><code>TERMIT_TEXTANALYSIS_TERMOCCURRENCEMINSCORE</code><b>&#42;</b></td>
        <td>
            Score threshold for a term occurrence for it to be saved into the repository.
            <br>
            Default value: <code>0.49</code><br>
            value must be present
        </td>
    </tr>
</table>

<b>&#42; Required</b>
