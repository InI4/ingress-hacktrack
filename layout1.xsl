<?xml version="1.0" encoding="UTF-8" ?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:hs="de.spieleck.ingress.hackstat"
                version="1.0">
  <xsl:output method="html" indent="no" />

  <xsl:template match="hs:hackstat">
     <html>
     <head>
     <title>Hackstat <xsl:value-of select="hs:created"/></title>
     <script type="text/javascript" src="https://www.google.com/jsapi"></script>
     <script type="text/javascript">
        google.load("visualization", "1", {packages:["corechart"]});
        function chartEl() { return document.getElementById('chart'); }
        function allEl() { return document.getElementById('all'); }
        function chartFun(title, data)
        {
            var data = google.visualization.arrayToDataTable(data);
            data.sort(1);
            var options = { 'title': title, width: 650, height: 400, is3D: true }; 
            var chart = new google.visualization.PieChart(document.getElementById('gChart'));
            chart.draw(data, options);
            chartEl().style.display = 'block';
            allEl().style.opacity = "0.3";
        }
        function closeChart()
        {
            chartEl().style.display = 'none';
            allEl().style.opacity = "1.0";
        }
      </script>
      <style type="text/css">
        td, th {
            font-family: arial, sans-serif, helvetica;
        }
        .chart {
            position: fixed;	
            top: 20%;
            left: 20%;
            width: 60%;
            height: 60%;
            z-index: 10;	
            display: none;
            padding: 16px;
            border: 1px solid #325580;
            background-color: white;
        }
        .gChart { 
            width: 650px;
            height: 400px;
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
      <div id="chart" class="chart" >
          <div id="gChart" class="gChart"> </div>
          <div style="float:right" ><a href="javascript:void(0)" onclick="closeChart();" >Close</a></div>
      </div>
      <div id="all">
      <h1><xsl:value-of select="hs:created"/></h1>
      <table>
      <xsl:for-each select="hs:value">
        <tr>
          <td class="first"><xsl:value-of select="hs:key" /> : </td>
          <td><xsl:value-of select="hs:*[2]" /></td>
        </tr>
      </xsl:for-each>
      </table>
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
                <!--
                <div class="small">
                    <xsl:value-of select="following::hs:description" />
                </div>
                -->
                <br />
                <a href="#top"><sup>^top</sup></a>
            </th>
            <td> </td>
            <xsl:for-each select="//hs:column">
                <xsl:variable name="colName" select="hs:key/text()" />
                <th class="filter" colspan="2">
                  <div style="vertical-align:top">
                    <a href="javascript:void(0)" style="float:top">
                      <xsl:attribute name="onclick">
                        <xsl:text>javascript:chartFun(</xsl:text>
                        <xsl:text>'</xsl:text>
                          <xsl:value-of select="$meStats" />
                          <xsl:text> </xsl:text>
                          <xsl:value-of select="hs:key" />
                        <xsl:text>',[</xsl:text>
                        <xsl:call-template name="js-data-row" >
                            <xsl:with-param name="meStats" select="$meStats" />
                            <xsl:with-param name="items" select="$items" />
                            <xsl:with-param name="meColumn" select="." />
                        </xsl:call-template>
                        <xsl:text>])</xsl:text>
                      </xsl:attribute>
                      <xsl:choose>
                        <xsl:when test="string-length(normalize-space(hs:key)) &gt; 0">
                            <xsl:value-of select="normalize-space(hs:key)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            ALL!
                        </xsl:otherwise>
                      </xsl:choose>
                    </a>
                  </div>
                  <xsl:variable name="av" select="//hs:hackstat/hs:column[hs:key/text() = $colName]/hs:stats[hs:key=$meStats]/hs:value[hs:key/text() = '_average']/hs:number" />
                  <xsl:if test="$av">
                    <div style="vertical-align:bottom">
                      mean=<xsl:value-of select="format-number($av,'0.00')" />
                    </div>
                  </xsl:if>
                </th>
            </xsl:for-each>
            <td> </td>
            <th class="rowleg">
                <xsl:attribute name="rowspan">
                    <xsl:value-of select="count($items) + 1" />
                </xsl:attribute>
              <xsl:value-of select="."/>
              <br />
              <a href="#top"><sup>^top</sup></a>
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
      </div>
      </body>
      </html>
  </xsl:template>


  <xsl:template name="js-data-row">
    <xsl:param name="meStats" />
    <xsl:param name="items" />
    <xsl:param name="meColumn" />
    <xsl:variable name="meColumnKey" select="$meColumn/hs:key" />
    <xsl:text>["",""]</xsl:text>
    <xsl:for-each select="$items" >
      <xsl:variable name="meKey" select="./hs:key" />
      <!--
      <xsl:variable name="val" select="//hs:column[hs:key=$meColumnKey]/hs:stats[hs:key=$meStats]/*[(name()='hs:item' or name()='hs:item2') and hs:key = $meKey]" />
      -->
      <xsl:variable name="val" select="$meColumn/hs:stats[hs:key=$meStats]/*[(name()='hs:item' or name()='hs:item2') and hs:key = $meKey]" />
      <xsl:if test="$val">
        <xsl:text>,["</xsl:text>
        <xsl:value-of select="$meKey" />
        <xsl:text>",</xsl:text>
        <xsl:choose>
          <xsl:when test="hs:absolute">
            <xsl:value-of select="hs:absolute" />
          </xsl:when>
          <xsl:when test="hs:average">
            <xsl:value-of select="round(hs:average)" />
          </xsl:when>
        </xsl:choose>
        <xsl:text>]</xsl:text>
      </xsl:if>
    </xsl:for-each>
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
