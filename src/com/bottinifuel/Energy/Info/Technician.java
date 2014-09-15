/*
 * Created on Feb 9, 2010 by pladd
 *
 */
package com.bottinifuel.Energy.Info;

/**
 * @author pladd
 *
 */
public class Technician
{
    public final String Alias;
    public final String Name;
    public final int    ID;
    
    protected Technician(int id, String name, String alias)
    {
        ID = id;
        Name = name;
        Alias = alias;
    }
    
    public ServiceCall CurrentCall()
    {
        return null;
    }
}
