from views import *
from django.conf.urls import patterns, include, url


urlpatterns = patterns( '',
			url(r'^$', home ),
			url(r'^ask/$', askPage ),
			)
