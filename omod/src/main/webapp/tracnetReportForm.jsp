<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/index.htm" />

<div id="page">
	<div id="container">
		<div id="wrapper">
		
			<h2>${reportDefinition.name}</h2>
			<span>${reportDefinition.description}</span>
			
			<br/><br/>
							
			<form id="tracnet-report-form" action="" method="POST">
				<input type="hidden" name="action" value="preview"/>
				<input type="hidden" name="uuid" value="${reportDefinition.uuid}"/>
				<input type="hidden" name="type" value="${reportDefinition.class.name}"/>

				<div>								
					<spring:hasBindErrors name="reportDefinition">  
						<li>
							<div class="errors"> 
								<font color="red"> 
									<h3><u>Please correct the following errors</u></h3>   									
									<growthchart_form:errors path="reportDefinition"></growthchart_form:errors>
								</font>  
							</div>
						</li>
					</spring:hasBindErrors>
				</div>
				<table>
					<tr>
						<td><label class="desc" for="startDate">Year</label>:</td>
						<td><input type="text" size="10" name="year" value="${year}"/></td>
					</tr>
					<tr>
						<td><label class="desc" for="endDate">Month</label>:</td>
						<td>
							<select name="month">
								<c:forEach items="${months}" var="monthEntry">
									<option value="${monthEntry.key}"<c:if test="${month == monthEntry.key}">selected</c:if>>${monthEntry.value}</option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td><label class="desc" for="format">Format</label>:</td>
						<td>
							<c:forEach items="${formats}" var="f">
								<input type="radio" name="format" value="${f}" <c:if test="${format == f}">selected</c:if>/>${f}
								&nbsp;&nbsp;&nbsp;
							</c:forEach>
						</td>
					</tr>				
				</table>
				<br/>
				<input class="button" id="submit-button" type="submit" value="Generate" />
			</form>	
				
		</div>
	</div>	
</div>
