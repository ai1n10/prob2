

<!DOCTYPE html>
<!--[if lt IE 7 ]><html class="ie ie6" lang="en"> <![endif]-->
<!--[if IE 7 ]><html class="ie ie7" lang="en"> <![endif]-->
<!--[if IE 8 ]><html class="ie ie8" lang="en"> <![endif]-->
<!--[if (gte IE 9)|!(IE)]><!-->
<html lang="en">
<!--<![endif]-->
<head>
<!-- Basic Page Needs
    ================================================== -->
<meta charset="utf-8">
<title>ProB Webshell</title>
<meta name="description" content="">
<meta name="author" content="Jens Bendisposto">

<!-- Mobile Specific Metas
    ================================================== -->
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">

<!-- CSS
    ================================================== -->
<link rel="stylesheet" href="stylesheets/base.css">
<link rel="stylesheet" href="stylesheets/skeleton.css">
<link rel="stylesheet" href="stylesheets/layout.css">
<link rel="stylesheet" href="stylesheets/evalb.css">
<link rel="stylesheet" href="stylesheets/pepper.css">

<style type="text/css">
 .axis path,
    .axis line {
        fill: none;
        stroke: black;
        shape-rendering: crispEdges;
}
    .axis text {
        font-family: sans-serif;
        font-size: 11px;
}

.connection {
 fill:none;
  stroke:black;
}


</style>

</head>
<body>

	<!-- Primary Page Layout
      ================================================== -->

	<!-- Delete everything in this .container and get started on your own site! -->

	<div class="container">
		<div id="show" class="sixteen columns">
		<form>
		Enter an Integer valued classical B formula:
		<input id="formula" name="formula"> 
		<input type="button" value="Show" onClick="javascript:displayFormula()"/>
		</form>
		<svg><svg>
		</div>
	</div>
	<!-- container -->

	<!-- JS
      ================================================== -->
	<!-- <script src="http://code.jquery.com/jquery-1.7.1.min.js"></script> -->
	<script src="javascripts/jquery-1.7.2.min.js"></script>
	<script src="javascripts/d3.v2.min.js"></script>
	<script src="javascripts/oszilloscope.js"></script>



	<!-- End Document
  ================================================== -->
</body>
</html>