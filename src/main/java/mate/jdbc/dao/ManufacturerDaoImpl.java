package mate.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import mate.jdbc.exceptions.DataProcessingException;
import mate.jdbc.lib.Dao;
import mate.jdbc.models.Manufacturer;
import mate.jdbc.util.ConnectionUtil;

@Dao
@Log4j2
public class ManufacturerDaoImpl implements ManufacturerDao {
    private static final String INSERT_QUERY =
            "INSERT INTO manufacturers(name, country) values(?, ?);";
    private static final String GET_BY_ID_QUERY =
            "SELECT * FROM manufacturers WHERE id = ?;";
    private static final String GET_ALL_QUERY =
            "SELECT * FROM manufacturers WHERE is_deleted = false;";
    private static final String UPDATE_QUERY =
            "UPDATE manufacturers SET name = ?, country = ? WHERE id = ?;";
    private static final String DELETE_QUERY =
            "UPDATE manufacturers SET is_deleted = true WHERE id = ?;";

    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        log.info("Method create was called with param: {}", manufacturer);
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement createStatement =
                        connection.prepareStatement(INSERT_QUERY,
                                Statement.RETURN_GENERATED_KEYS)) {
            createStatement.setString(1, manufacturer.getName());
            createStatement.setString(2, manufacturer.getCountry());
            createStatement.executeUpdate();
            ResultSet generatedKeys = createStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long id = generatedKeys.getObject(1, Long.class);
                manufacturer.setId(id);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't insert manufacturer to db " + manufacturer, e);
        }
        return manufacturer;
    }

    @Override
    public Optional<Manufacturer> getById(Long id) {
        log.info("Method getById was called with param: {}", id);
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement getByIdStatement =
                        connection.prepareStatement(GET_BY_ID_QUERY)) {
            getByIdStatement.setObject(1, id);
            ResultSet resultSet = getByIdStatement.executeQuery();
            if (resultSet.next()) {
                Manufacturer manufacturer = Manufacturer.builder()
                        .country(resultSet.getString("country"))
                        .name(resultSet.getString("name"))
                        .id(resultSet.getObject(1, Long.class))
                        .build();
                return Optional.of(manufacturer);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get manufacturer by id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Manufacturer> getAll() {
        log.info("Method getAll was called");
        List<Manufacturer> manufacturers = new ArrayList<>();
        try (Connection connection = ConnectionUtil.getConnection();
                Statement getAllStatement = connection.createStatement()) {
            ResultSet resultSet = getAllStatement.executeQuery(GET_ALL_QUERY);
            while (resultSet.next()) {
                Manufacturer manufacturer = Manufacturer.builder()
                        .country(resultSet.getString("country"))
                        .name(resultSet.getString("name"))
                        .id(resultSet.getObject(1, Long.class))
                        .build();
                manufacturers.add(manufacturer);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get all manufacturers from db", e);
        }
        return manufacturers;
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        log.info("Method update was called with param: {}", manufacturer);
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_QUERY)) {
            statement.setString(1, manufacturer.getName());
            statement.setString(2, manufacturer.getCountry());
            statement.setObject(3, manufacturer.getId());
            return statement.executeUpdate() >= 1 ? manufacturer : null;
        } catch (SQLException e) {
            throw new DataProcessingException("can't update manufacturer " + manufacturer, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        log.info("Method delete was called with param: {}", id);
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_QUERY)) {
            statement.setObject(1, id);
            return statement.executeUpdate() >= 1;
        } catch (SQLException e) {
            throw new DataProcessingException("Can't delete manufacturer by id " + id, e);
        }
    }
}