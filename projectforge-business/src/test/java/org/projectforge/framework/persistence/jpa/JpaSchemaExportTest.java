/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.framework.persistence.jpa;

import de.micromata.genome.util.bean.PrivateBeanUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.boot.spi.MetadataImplementor;
import org.junit.jupiter.api.Test;
import org.projectforge.test.AbstractTestBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JpaSchemaExportTest extends AbstractTestBase
{
  @Test
  public void testExport()
  {
    Map<String, String> props = new HashMap<>();
    props.put("javax.persistence.schema-generation.scripts.action", "drop-and-create");
    props.put("javax.persistence.schema-generation.scripts.create-target", "target/pfcreate.sql");
    props.put("javax.persistence.schema-generation.scripts.drop-target", "target/pfdrop.sql");
    PfEmgrFactory pf = PfEmgrFactory.get();
    SessionFactory sf = pf.getEntityManagerFactory().unwrap(SessionFactory.class);
    Object sreg = PrivateBeanUtils.readField(PrivateBeanUtils.readField(sf, "serviceRegistry"), "parent");
    //    StandardServiceRegistry serviceRegistry = buildStandardServiceRegistry();
    StandardServiceRegistry serviceRegistry = (StandardServiceRegistry) sreg;
    MetadataImplementor metadata = buildMetadata(serviceRegistry);

/*    SchemaExport schemaExport = new SchemaExport(serviceRegistry, metadata, true)
        .setHaltOnError(true)
        .setOutputFile("target/pf2out.sql")
        .setDelimiter(";\n")
        //        .setImportSqlCommandExtractor(serviceRegistry.getService(ImportSqlCommandExtractor.class))
        .setFormat(true);
    schemaExport.execute(
        true,
        true,
        false,
        false);
*/
    //    Persistence.generateSchema(PfEmgrFactory.get().getUnitName(), props);
  }

  private static MetadataImplementor buildMetadata(
      StandardServiceRegistry serviceRegistry)
  {
    final MetadataSources metadataSources = new MetadataSources(serviceRegistry);

    final MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();
    final StrategySelector strategySelector = serviceRegistry.getService(StrategySelector.class);

    metadataBuilder.applyImplicitNamingStrategy(
        strategySelector.resolveStrategy(
            ImplicitNamingStrategy.class,
            null));

    return (MetadataImplementor) metadataBuilder.build();
  }

  private static StandardServiceRegistry buildStandardServiceRegistry()

  {
    final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
    final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder(bsr);

    Properties properties = new Properties();
    ssrBuilder.applySettings(properties);

    return ssrBuilder.build();
  }
}
