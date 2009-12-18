<?xml version="1.0" encoding="utf-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes" name="xml"/>
    
    <xsl:template match="@*|node()" priority="2">
      <xsl:choose>
        <xsl:when test="local-name()='edge'">
           <xsl:value-of select="data/."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
          </xsl:copy>
        </xsl:otherwise>
      </xsl:choose>

    </xsl:template>

</xsl:stylesheet>
