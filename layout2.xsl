<?xml version="1.0" encoding="UTF-8" ?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:hs="de.spieleck.ingress.hackstat"
                version="1.0">
  <xsl:output method="html" indent="no" />

  <xsl:template match="hs:hackstat">
      <html>
      <head>
      <style type="text/css">
        td, th {
            font-family: arial, sans-serif, helvetica;
        }
        .header {
          background: #64Aec0;
          align: center;
        }
        .data {
          font-size:11px;
          text-align: right;
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
      </style>
      </head>
      <body>
      <!-- Does not work with Java7, but with old Saxon?! -->
      <xsl:variable name="all-stats" select="hs:column/hs:stats/hs:key[not(text()=preceding::hs:stats/hs:key/text())]" />
      <!--
      <xsl:for-each select="$all-stats"><xsl:value-of select="."/>, </xsl:for-each>
      -->
      <table>
      <tr class="header">
        <th colspan="2">Filter ==&gt; </th>
        <xsl:for-each select="hs:column">
          <th colspan="2"><xsl:value-of select="hs:key"/></th>
        </xsl:for-each>
        <th colspan="2">&lt;== Filter</th>
      </tr>
      <xsl:for-each select="$all-stats">
          <xsl:variable name="meStats" select="." />
          <!-- <xsl:variable name="items" select="//hs:hackstat/hs:column/hs:stats[hs:key=$meStats]/hs:*[(local-name()='item' or local-name()='item2') and not(hs:key/text()=preceding::hs:stats[hs:key=$meStats]/hs:*[local-name()='item' or local-name()='item2']/hs:key/text())]" /> -->
          <xsl:variable name="items" select="//hs:hackstat/hs:column/hs:stats[hs:key=$meStats]/*[(name() = 'hs:item' or name() = 'hs:item2') and not(hs:key/text()=preceding::hs:stats[hs:key=$meStats]/*[name()='hs:item' or name()='hs:item2']/hs:key/text())]" />
          <tr class="firstRow">
            <xsl:call-template name="data-row-leg">
                <xsl:with-param name="items" select="$items" />
            </xsl:call-template>
            <xsl:call-template name="data-row" >
                <xsl:with-param name="meItem" select="$items[1]" />
                <xsl:with-param name="meStats" select="$meStats" />
            </xsl:call-template>
            <xsl:call-template name="data-row-leg">
                <xsl:with-param name="items" select="$items" />
            </xsl:call-template>
          </tr>
          <xsl:for-each select="$items[position() > 1]" >
              <tr>
                  <xsl:call-template name="data-row" >
                      <xsl:with-param name="meItem" select="." />
                      <xsl:with-param name="meStats" select="$meStats" />
                  </xsl:call-template>
              </tr>
          </xsl:for-each>
      </xsl:for-each>
      <tr class="header">
        <th colspan="2">Filter ==&gt; </th>
          <xsl:for-each select="hs:column">
        <th colspan="2"><xsl:value-of select="hs:key"/></th>
        </xsl:for-each>
        <th colspan="2">&lt;== Filter</th>
      </tr>
      </table>
      </body>
      </html>
  </xsl:template>

  <xsl:template name="data-row-leg">
    <xsl:param name="items" />
    <th class="rowleg">
      <xsl:attribute name="rowspan">
          <xsl:value-of select="count($items)" />
      </xsl:attribute>
      <xsl:value-of select="."/>
    </th>
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
            <td class="data absolute">
                <xsl:value-of select="$me/hs:absolute" />
            </td>
            <td class="data percentage">
              <xsl:value-of select="round($me/hs:percentage*10) div 10" />%
            </td>
          </xsl:when>
          <xsl:when test="$me/hs:average">
            <td class="data percentage">
                <xsl:value-of select="round($me/hs:average)" />
            </td>
            <td class="data percentage nobr">
                [<xsl:value-of select="round($me/hs:min*10) div 10" />;
                <xsl:value-of select="round($me/hs:max*10) div 10" />]%
            </td>
          </xsl:when>
          <xsl:otherwise>
              <td class="data">-</td><td class="data">-</td>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
    <th class="rowleg2">
        <xsl:value-of select="$meKey"/>
    </th>
</xsl:template>

</xsl:stylesheet>
