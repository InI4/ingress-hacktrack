<?xml version="1.0" encoding="UTF-8" ?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:hs="de.spieleck.ingress.hackstat"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="hs xsl"
                version="1.0">
                <xsl:output method="html" indent="no" encoding="utf-8" doctype-public="-//W3C/DTD XHTML 1.0 Strict/EN" doctype-system="http://www.w3.org/TR/xhtml/DTD/xhtml1-strict.dtd" />
  <xsl:param name="filter" select="''" />
  <xsl:param name="antifilter" select="'NEUTRAL'" />
  <xsl:param name="SPC" select="' '" />

  <xsl:template match="hs:hackstat">
     <xsl:message>Filter=<xsl:value-of select="$filter"/>-<xsl:value-of select="$antifilter"/></xsl:message>
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
        function showHide(id, mode)
        {
            var els = document.getElementsByClassName('sh_'+id);
            var nst = els[0].style.display == 'none' || mode=='show' ? '' : 'none';
            for(i = 0; i &lt; els.length; i++) {
                 els[i].style.display = nst;
            }
            var el2 = document.getElementsByClassName('th_'+id);
            for(i = 0; i &lt; el2.length; i++) {
                el2[i].innerHTML = nst == 'none' ? 'show details' : 'hide details';
            }
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
        .evil {
          background-color: #63EF21;
        }
        .rowleg, .rowleg2 {
            vertical-align: text-top;
            background: #86C1FF;
        }
        .firstRow {
            background: #64AeFF;
            border-top: black 2px solid;
        }
        .firstRow TH {
                  padding: 5pt;
        }
        .secondRow, .thirdRow {
            background: #86C1FF;
            border-top: black 2px solid;
        }
        .secondRow TH { 
            padding: 3pt;
        }
        .thirdRow TH { 
            padding: 2pt;
            font-weight: normal;
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
        .red {
            color:red;
        }
        .yellow {
            color:yellow;
        }
        .green {
            color:green;
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
      <!-- Does not work with Java7, but with old Saxon?! -->
      <xsl:variable name="all-stats" select="hs:column/hs:stats/hs:key[not(text()=preceding::hs:stats/hs:key/text())]" /> 
      <table>
      <xsl:for-each select="hs:value">
        <tr>
          <td class="first nobr"><xsl:value-of select="hs:key" /> : </td>
          <td><xsl:value-of select="hs:*[2]" /></td>
        </tr>
      </xsl:for-each>
        <tr>
          <td class="first nobr">Statistics : </td>
          <td>
            <xsl:for-each select="$all-stats">
                <xsl:variable name="meStats" select="." />
                <xsl:variable name="id" select="generate-id($meStats)" />
                <a href="#{$id}" onclick="showHide('{$id}', 'show');"><xsl:value-of select="."/></a>
                <xsl:text>, </xsl:text>
            </xsl:for-each>
          </td>
        </tr>
      </table>
      <table>
      <xsl:for-each select="$all-stats">
          <xsl:variable name="meStats" select="." />
          <xsl:variable name="id" select="generate-id($meStats)" />
          <!-- <xsl:variable name="items" select="//hs:hackstat/hs:column/hs:stats[hs:key=$meStats]/hs:*[(local-name()='item' or local-name()='item2') and not(hs:key/text()=preceding::hs:stats[hs:key=$meStats]/hs:*[local-name()='item' or local-name()='item2']/hs:key/text())]" /> -->
          <xsl:variable name="items" select="//hs:hackstat/hs:column/hs:stats[hs:key=$meStats]/*[(name() = 'hs:item' or name() = 'hs:item2') and not(hs:key/text()=preceding::hs:stats[hs:key=$meStats]/*[name()='hs:item' or name()='hs:item2']/hs:key/text())]" />
          <tr class="firstRow">
            <th class="rowleg" rowspan="3">
                <a name="{$id}" />
                <xsl:value-of select="."/>
                <br />
                <br />
                <a href="javascript:void(0)" onclick="showHide('{$id}','?');" ><sup class='th_{$id}'>show details</sup></a>
                <br />
                <br />
                <a href="#top"><sup>^top</sup></a>
            </th>
            <td rowspan="3" class="nobr">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</td>
            <xsl:variable name="metacolumns"
                select="hs:column/hs:stats/hs:key[not(text()=preceding::hs:stats/hs:key/text())]" /> 
            <xsl:for-each select="//hs:column[contains(hs:key/text(),$filter) and not(contains(hs:key/text(),$antifilter))]">
                <xsl:variable name="theKey"><xsl:call-template name="findColumnKey" /></xsl:variable>
                <xsl:variable name="keyPart1" select="substring-before($theKey, $SPC)" />
                <xsl:if test="substring-before(hs:key/text(),$SPC) != substring-before(preceding-sibling::*[1]/hs:key/text(),$SPC)" >
                    <th align="left">
                        <xsl:attribute name="colspan">
                            <xsl:value-of select="2 * count(../hs:column[contains(hs:key/text(),$filter) and not(contains(hs:key/text(),$antifilter)) and starts-with(hs:key/text(), concat($keyPart1,' '))])" />
                        </xsl:attribute>
                        &#160;<xsl:value-of select="$keyPart1" /><br />
                    </th>
                  </xsl:if>
            </xsl:for-each>
            <td rowspan="3" class="nobr">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</td>
            <th class="rowleg" rowspan="3">
              <xsl:value-of select="."/>
              <br />
              <br />
              <a href="javascript:void(0)" onclick="showHide('{$id}','?');" ><sup class="th_{$id}">show details</sup></a>
              <br />
              <br />
              <a href="#top"><sup>^top</sup></a>
            </th>
            </tr><tr class="secondRow">
            <xsl:for-each select="//hs:column[contains(hs:key/text(),$filter) and not(contains(hs:key/text(),$antifilter))]">
                <xsl:variable name="colName" select="hs:key/text()" />
                <xsl:variable name="theKey"><xsl:call-template name="findColumnKey" /></xsl:variable>
                <th colspan="2">
                  <xsl:attribute name="class">
                      <xsl:text>filter</xsl:text>
                      <xsl:if test="not(contains(hs:key/text(), 'FRIEND'))"> evil</xsl:if>
                  </xsl:attribute>
                    <a href="javascript:void(0)" rubarb="float:top">
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
                      <xsl:value-of select="substring-after($theKey, $SPC)" />
                    </a>
                 </th>
            </xsl:for-each>
            </tr><tr class="thirdRow">
            <xsl:for-each select="//hs:column[contains(hs:key/text(),$filter) and not(contains(hs:key/text(),$antifilter))]">
                <xsl:variable name="colName" select="hs:key/text()" />
                <th colspan="2"> 
                  <xsl:attribute name="class">
                      <xsl:text>filter</xsl:text>
                      <xsl:if test="not(contains(hs:key/text(), 'FRIEND'))"> evil</xsl:if>
                  </xsl:attribute>
                  <xsl:variable name="average" select="//hs:hackstat/hs:column[hs:key/text() = $colName]/hs:stats[hs:key=$meStats]/hs:value[hs:key/text() = '_average']/hs:number" />
                  <xsl:if test="$average">
                    mean=<xsl:value-of select="format-number($average,'0.00')" />
                  </xsl:if>
                  <xsl:variable name="sdev" select="//hs:hackstat/hs:column[hs:key/text() = $colName]/hs:stats[hs:key=$meStats]/hs:value[hs:key/text() = '_sdev']/hs:number" />
                  <xsl:if test="$sdev">
                    <br />+/- <xsl:value-of select="format-number($sdev,'0.00')" />
                  </xsl:if>
                  <xsl:variable name="changePerc" select="//hs:hackstat/hs:column[hs:key/text() = $colName]/hs:stats[hs:key=$meStats]/hs:value[hs:key/text() = '_changePerc']/hs:string" />
                  <xsl:if test="$changePerc">
                    <br /><span>
                      <xsl:attribute name="class">
                          nobr
                          <xsl:choose>
                            <xsl:when test="$changePerc > 95">red</xsl:when>
                            <xsl:when test="$changePerc > 80">yellow</xsl:when>
                            <xsl:when test="$changePerc > 50">green</xsl:when>
                          </xsl:choose>
                      </xsl:attribute>
                      change: <xsl:value-of select="$changePerc" />%
                    </span>
                  </xsl:if>
                </th>
            </xsl:for-each>
          </tr>
          <xsl:for-each select="$items" >
            <xsl:sort select="./hs:key" data-type="number"/>
            <tr class='sh_{$id}' style='display : none' >
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

  <xsl:template name="findColumnKey">
      <xsl:choose>
          <xsl:when test="string-length(normalize-space(hs:key)) &gt; 0">
              <xsl:value-of select="normalize-space(hs:key)"/>
          </xsl:when>
          <xsl:otherwise>
              EVER ALL 
            </xsl:otherwise>
      </xsl:choose>
  </xsl:template>

  <xsl:template name="js-data-row">
    <xsl:param name="meStats" />
    <xsl:param name="items" />
    <xsl:param name="meColumn" />
    <xsl:variable name="meColumnKey" select="$meColumn/hs:key" />
    <xsl:text>["",""]</xsl:text>
    <xsl:for-each select="$items" >
      <xsl:sort select="./hs:key" data-type="number"/>
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
    <td>&#160;</td>
    <th class="rowleg2 nobr" >
        <xsl:value-of select="$meKey"/>
    </th>
    <!-- <xsl:for-each select="//hs:column"> -->
    <xsl:for-each select="//hs:column[contains(hs:key/text(),$filter) and not(contains(hs:key/text(),$antifilter))]">
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
    <th class="rowleg2 nobr">
        <xsl:value-of select="$meKey"/>
    </th>
    <td>&#160;</td>
</xsl:template>

</xsl:stylesheet>
