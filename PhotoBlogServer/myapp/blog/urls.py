from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import blogImage, PersonPostViewSet
from . import views

router_root = DefaultRouter()
router_root.register(r'Post', blogImage, basename='post-root')

router_api = DefaultRouter()
router_api.register(r'posts', blogImage, basename='post-api')

router_person = DefaultRouter()
router_person.register(r'posts', PersonPostViewSet, basename='post-person')

urlpatterns = [
    path('', views.post_list, name='post_list'),
    path('api_root/', include(router_root.urls)),  # /api_root/Post/
    path('api/', include(router_api.urls)),        # /api/posts/ new task api
    path('post/<int:pk>/', views.post_detail, name='post_detail'),
    path('post/new/', views.post_new, name='post_new'),
    path('post/<int:pk>/edit/', views.post_edit, name='post_edit'),
    path('api_person/', include(router_person.urls)),
]