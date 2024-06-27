package com.example.ventasandusers.ui.gallery;

import java.util.Objects;

public class Articulo {

    String nombre;
    double precion;
    int imagenArticulo;

    //Constructores, getters y setters

    public Articulo() {
    }

    public Articulo(String nombre, double precion, int imagenArticulo) {
        this.nombre = nombre;
        this.precion = precion;
        this.imagenArticulo = imagenArticulo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecion() {
        return precion;
    }

    public void setPrecion(double precion) {
        this.precion = precion;
    }

    public int getImagenArticulo() {
        return imagenArticulo;
    }

    public void setImagenArticulo(int imagenArticulo) {
        this.imagenArticulo = imagenArticulo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Articulo articulo = (Articulo) o;
        return Double.compare(articulo.precion, precion) == 0 && imagenArticulo == articulo.imagenArticulo && Objects.equals(nombre, articulo.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, precion, imagenArticulo);
    }
}
