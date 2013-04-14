import SPARQLWrapper
from rdflib import URIRef, Literal, Graph #NOTE: RDF in rdflib and SPARQLWrapper gets conficts
from panorama.Selector import *
from panorama.Formatter import *
from panorama.Fresnel import *

from django.template import RequestContext
from django.shortcuts import render_to_response
from django.http import HttpResponse

def queryEndpoint( query ):
        sparql = SPARQLWrapper.SPARQLWrapper("http://dbpedia.org/sparql")
        sparql.setQuery( query )
        sparql.setReturnFormat( SPARQLWrapper.RDF )
        result = sparql.query().convert()
        return result


def extractSolutions( graph ):
        """This function returns the subject of sparql-solutions from the graph"""

        solutions = []

	#for result in graph.subjects(predicate=URIRef('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), object=URIRef('http://www.w3.org/2005/sparql-results#ResultSet')):
	 #       for solution in graph.objects( subject=result, predicate=URIRef('http://www.w3.org/2005/sparql-results#solution')):
	 #               solutions.append( solution )

	for node in graph.subjects( predicate=URIRef(u'http://www.w3.org/2005/sparql-results#variable'), object=Literal(u'uri') ):
		uri = graph.value( subject=node, predicate=URIRef(u'http://www.w3.org/2005/sparql-results#value') )
		solutions.append( uri )

        return solutions

def extractUris( graph, solution_nodes ):
	pass

# Views Start Here

def home(request):
	return render_to_response( 'home.html', {}, context_instance=RequestContext(request) )

def askPage(request):
	
	#TODO: from question get the SPARQL Query, integrate with backend

	SPARQL = """PREFIX yago: <http://dbpedia.org/class/yago/>
		    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		    SELECT DISTINCT ?uri ?string
		    WHERE {
			?uri rdf:type yago:RussianCosmonauts .
		        ?uri rdf:type yago:FemaleAstronauts .
			OPTIONAL { ?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }
		  } """
	
	SPARQL1 = """PREFIX dbo: <http://dbpedia.org/ontology/>
		     PREFIX res: <http://dbpedia.org/resource/>
		     PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		     SELECT DISTINCT ?uri ?string 
		     WHERE {       
     			   res:Brooklyn_Bridge dbo:crosses ?uri . 
      			   OPTIONAL { ?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }
		 }"""
	print "SPARQL query...", SPARQL

	print "Querying endpoint..."
	#Query the endpoint and get result
	result_graph = queryEndpoint( SPARQL )

	print result_graph

	print "Result..."
	for term in result_graph:
		print term

	print "Extracting solution nodes from result..."
	#Parse the resultset and get the URIs
	uris = extractSolutions( result_graph )
	
	print "Solutions..."

	for node in uris:
		print node

	#Using panorama
	solution_graph = Graph()
	print "Downloading resources..."
	#TODO: check efficiency
	for uri in uris:
		print "for uri ", uri
		solution_graph.parse( uri )
	
	print "RDF Graph..."
	for term in solution_graph:
		print term
	
        print "Creating Fresnel Graph.."
	f = open('panorama/data/person_foaf.n3')
        fresnel_data = f.read()
        fresnel = Fresnel( fresnel_data )

	print "Making selection.."
        selector = Selector( fresnel , solution_graph)
        selector.select()
	print "Formatting..."
        formatter = Formatter( selector )
        formatter.format()
	data = "<html>\n<head><link rel='stylesheet' type='text/css' href='/static/style.css'></head>\n<body>\n"
        for resource in formatter.result:
                data += resource.render()
        data += "</body>\n</html>"
	return HttpResponse( data )

