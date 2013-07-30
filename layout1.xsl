<?xml version="1.0" encoding="UTF-8" ?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:hs="de.spieleck.ingress.hackstat"
                version="1.0">
  <xsl:output method="html" indent="no" />

  <xsl:template match="hs:hackstat">
      <html>
      <head>
      <title>Hackstat <xsl:value-of select="hs:created"/></title>
      <style type="text/css">
        td, th {
            font-family: arial, sans-serif, helvetica;
        }
        .data {
          font-size:8pt;
          text-align: right;
        }
        .filter {
          font-size:9pt;
        }
        .rowleg, .rowleg2 {
            vertical-align: text-top;
            background: #86C1FF;
        }
        .firstRow {
            background: #64AeFF;
            border-top: black 2px solid;
        }
        .nobr {
            white-space:nowrap;
        }
        .first {
            background: #A6E1FF;
        }
        .percentage {
            text-align: center;
        }
        .small {
            font-size:9pt;
            font-weight: normal;
        }
      </style>
      </head>
      <body>
      <a name="top" />
      <h1><xsl:value-of select="hs:created"/></h1>
      <!-- Does not work with Java7, but with old Saxon?! -->
      <xsl:variable name="all-stats" select="hs:column/hs:stats/hs:key[not(text()=preceding::hs:stats/hs:key/text())]" />
      Statistics: 
      <xsl:for-each select="$all-stats">
          <xsl:variable name="meStats" select="." />
          <a>
              <xsl:attribute name="href">
                  <xsl:text>#</xsl:text>
                  <xsl:value-of select="generate-id($meStats)" />
              </xsl:attribute>
              <xsl:value-of select="."/>
          </a>
          <xsl:text>, </xsl:text>
      </xsl:for-each>
      <table>
      <xsl:for-each select="$all-stats">
          <xsl:variable name="meStats" select="." />
          <!-- <xsl:variable name="items" select="//hs:hackstat/hs:column/hs:stats[hs:key=$meStats]/hs:*[(local-name()='item' or local-name()='item2') and not(hs:key/text()=preceding::hs:stats[hs:key=$meStats]/hs:*[local-name()='item' or local-name()='item2']/hs:key/text())]" /> -->
          <xsl:variable name="items" select="//hs:hackstat/hs:column/hs:stats[hs:key=$meStats]/*[(name() = 'hs:item' or name() = 'hs:item2') and not(hs:key/text()=preceding::hs:stats[hs:key=$meStats]/*[name()='hs:item' or name()='hs:item2']/hs:key/text())]" />
          <tr class="firstRow">
            <th class="rowleg">
                <xsl:attribute name="rowspan">
                    <xsl:value-of select="count($items) + 1" />
                </xsl:attribute>
                <a>
                  <xsl:attribute name="name">
                      <xsl:value-of select="generate-id($meStats)" />
                  </xsl:attribute>
                </a>
                <xsl:value-of select="."/>
                <div class="small">
                    <xsl:value-of select="following::hs:description" />
                </div>
                <br />
                <a href="#top"><sub>^</sub></a>
            </th>
            <td> </td>
            <xsl:for-each select="//hs:column">
                <th class="filter" colspan="2"><xsl:value-of select="hs:key"/></th>
            </xsl:for-each>
            <td> </td>
            <th class="rowleg">
                <xsl:attribute name="rowspan">
                    <xsl:value-of select="count($items) + 1" />
                </xsl:attribute>
              <xsl:value-of select="."/>
              <br />
              <a href="#top"><sub>^</sub></a>
            </th>
          </tr>
          <xsl:for-each select="$items" >
              <tr>
                  <xsl:call-template name="data-row" >
                      <xsl:with-param name="meItem" select="." />
                      <xsl:with-param name="meStats" select="$meStats" />
                  </xsl:call-template>
              </tr>
          </xsl:for-each>
      </xsl:for-each>
      </table>
      </body>
      </html>
  </xsl:template>

  <xsl:template name="data-row">
    <xsl:param name="meItem" />
    <xsl:param name="meStats" />
    <xsl:variable name="meKey" select="$meItem/hs:key" />
    <th class="rowleg2">
        <xsl:value-of select="$meKey"/>
    </th>
    <xsl:for-each select="//hs:column">
        <xsl:variable name="me" select="hs:stats[hs:key=$meStats]/*[(name()='hs:item' or name()='hs:item2') and hs:key = $meKey]" />
        <xsl:choose>
          <xsl:when test="$me/hs:absolute">
            <td class="data first absolute">
                <xsl:value-of select="$me/hs:absolute" />
            </td>
            <td class="data percentage">
              <xsl:value-of select="round($me/hs:percentage*10) div 10" />%
            </td>
          </xsl:when>
          <xsl:when test="$me/hs:average">
            <td class="data first percentage">
                <xsl:value-of select="round($me/hs:average)" />
            </td>
            <td class="data percentage nobr">
                <xsl:value-of select="round($me/hs:min*10) div 10" />-<xsl:value-of select="round($me/hs:max*10) div 10" />%
            </td>
          </xsl:when>
          <xsl:otherwise>
              <td class="first data">-</td><td class="data">-</td>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
    <th class="rowleg2">
        <xsl:value-of select="$meKey"/>
    </th>
</xsl:template>

</xsl:stylesheet>
